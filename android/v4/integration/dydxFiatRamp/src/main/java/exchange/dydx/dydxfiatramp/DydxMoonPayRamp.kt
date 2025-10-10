package exchange.dydx.dydxfiatramp

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.moonpay.sdk.MoonPayAndroidSdk
import com.moonpay.sdk.MoonPayBuyQueryParams
import com.moonpay.sdk.MoonPayHandlers
import com.moonpay.sdk.MoonPayRenderingOptionAndroid
import com.moonpay.sdk.MoonPaySdkBuyConfig
import com.moonpay.sdk.MoonPayWidgetEnvironment
import com.moonpay.sdk.OnInitiateDepositResponsePayload
import exchange.dydx.abacus.functional.ClientTrackableEventType
import exchange.dydx.trading.feature.shared.analytics.logSharedEvent
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import exchange.dydx.utilities.utils.Logging
import kotlinx.serialization.Serializable
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

private val TAG = "DydxMoonPayRamp"
private val PROVIDER = "moonpay"

@Singleton
class DydxMoonPayRamp @Inject constructor(
    private val logger: Logging,
    private val tracker: Tracking,
) {
    private var moonPaySdk: MoonPayAndroidSdk? = null

    fun takeActivity(activity: ComponentActivity) {
        // Initialize the SDK using 'this' reference for MainActivity
        // This needs to be called from an Activity's onCreate() method
        moonPaySdk = MoonPayAndroidSdk(activity = activity)
    }

    fun show(targetAddress: String, usdAmount: Double?, config: DydxMoonPayConfig, completion: ((Boolean, String?) -> Unit)) {
        fun handleError(msg: String) {
            completion.invoke(false, msg)
            logger.e(TAG, msg)
            tracker.logSharedEvent(
                ClientTrackableEventType.FiatDepositRouteToProviderErrorEvent(
                    message = msg,
                    provider = PROVIDER,
                ),
            )
        }

        tracker.logSharedEvent(
            ClientTrackableEventType.FiatDepositShowInputEvent(),
        )

        if (moonPaySdk == null) {
            handleError("MoonPay SDK is not initialized. Call takeActivity() first.")
            return
        }

        val handlers = MoonPayHandlers(
            onSwapsCustomerSetupComplete = {
                Log.i("HANDLER CALLED", "onSwapsCustomerSetupComplete called!")
                tracker.logSharedEvent(
                    ClientTrackableEventType.FiatDepositMoonPayCallbackEvent(
                        callbackName = "onSwapsCustomerSetupComplete",
                        data = mapOf(),
                    ),
                )
            },
            onAuthToken = {
                Log.i("HANDLER CALLED", "onAuthToken called with payload $it")
                tracker.logSharedEvent(
                    ClientTrackableEventType.FiatDepositMoonPayCallbackEvent(
                        callbackName = "onAuthToken",
                        data = mapOf(),
                    ),
                )
            },
            onLogin = {
                Log.i("HANDLER CALLED", "onLogin called with payload $it")
                tracker.logSharedEvent(
                    ClientTrackableEventType.FiatDepositMoonPayCallbackEvent(
                        callbackName = "onLogin",
                        data = mapOf(
                            "isRefresh" to it.isRefresh,
                        ),
                    ),
                )
            },
            onInitiateDeposit = {
                tracker.logSharedEvent(
                    ClientTrackableEventType.FiatDepositMoonPayCallbackEvent(
                        callbackName = "onInitiateDeposit",
                        data = mapOf(
                            "fiatCurrency" to it.fiatCurrency,
                            "fiatCurrencyAmount" to (it.fiatCurrencyAmount ?: "0"),
                            "cryptoCurrency" to it.cryptoCurrency,
                            "depositWalletAddress" to it.depositWalletAddress,
                            "transactionId" to it.transactionId,
                            "cryptoCurrencyAmount" to it.cryptoCurrencyAmount,
                            "cryptoCurrencyAmountSmallestDenomination" to it.cryptoCurrencyAmountSmallestDenomination,
                        ),
                    ),
                )
                OnInitiateDepositResponsePayload(depositId = "someDepositId")
            },
            onKmsWalletCreated = {
                tracker.logSharedEvent(
                    ClientTrackableEventType.FiatDepositMoonPayCallbackEvent(
                        callbackName = "onKmsWalletCreated",
                        data = mapOf(),
                    ),
                )
            },
            onUnsupportedRegion = {
                tracker.logSharedEvent(
                    ClientTrackableEventType.FiatDepositMoonPayCallbackEvent(
                        callbackName = "onUnsupportedRegion",
                        data = mapOf(),
                    ),
                )
            },
        )

        val params = MoonPayBuyQueryParams(apiKey = config.moonPayPk)
        params.setBaseCurrencyCode("USD")
        params.setBaseCurrencyAmount(usdAmount)
        params.setTheme(if (config.isDarkTheme) "dark" else "light")
        params.setCurrencyCode("usdc_noble")
        params.setWalletAddress(targetAddress)

        val sdkConfig = MoonPaySdkBuyConfig(
            environment = if (config.isSandbox) MoonPayWidgetEnvironment.Sandbox else MoonPayWidgetEnvironment.Production,
            debug = false,
            params = params,
            handlers = handlers,
        )

        moonPaySdk?.updateConfig(sdkConfig)

        val url = moonPaySdk?.generateUrlForSigning()
        if (url == null) {
            handleError("Failed to generate URL for signing")
            return
        }

        val components = url.split("?")
        if (components.size != 2) {
            handleError("Invalid URL format")
            return
        }

        val query = components[1]
        val queryPath = "?$query".toByteArray(Charsets.UTF_8)
        getSignature(queryPath, config) { signature, error ->
            if (error != null) {
                handleError("Failed to get signature: ${error.message}")
                return@getSignature
            }
            if (signature == null) {
                handleError("Signature is null")
                return@getSignature
            }
            moonPaySdk?.updateSignature(signature)
            moonPaySdk?.show(MoonPayRenderingOptionAndroid.InAppBrowser)

            tracker.logSharedEvent(
                ClientTrackableEventType.FiatDepositRouteToProviderCompletedEvent(
                    amountUsd = usdAmount,
                    depositAddress = targetAddress,
                    provider = PROVIDER,
                ),
            )

            completion(true, null)
        }
    }

    private fun getSignature(encodedUrlData: ByteArray, config: DydxMoonPayConfig, completionHandler: ((String?, Error?) -> Unit)) {
        if (config.isSandbox) {
            if (config.moonPaySk != null) {
                val signature =
                    generateSignature(moonPaySk = config.moonPaySk, encodedUrlData = encodedUrlData)
                completionHandler(signature, null)
            } else {
                completionHandler(null, Error("MoonPay SK is null"))
            }
        } else {
            if (config.moonPaySignUrl != null) {
                getRemoteSignature(
                    encodedUrlData = encodedUrlData,
                    url = config.moonPaySignUrl,
                    completion = completionHandler,
                )
            } else {
                completionHandler(null, Error("MoonPay sign URL is null"))
            }
        }
    }

    private fun generateSignature(moonPaySk: String, encodedUrlData: ByteArray): String {
        val key = SecretKeySpec(moonPaySk.toByteArray(Charsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(key)

        val signatureBytes = mac.doFinal(encodedUrlData)
        return Base64.getEncoder().encodeToString(signatureBytes)
    }

    private val client = OkHttpClient()
    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun getRemoteSignature(
        encodedUrlData: ByteArray,
        url: String,
        completion: (String?, Error?) -> Unit
    ) {
        val pathData = Base64.getEncoder().encodeToString(encodedUrlData)
        val urlString = "$url?path=$pathData"

        val request = try {
            Request.Builder()
                .url(urlString)
                .build()
        } catch (e: Exception) {
            completion(null, Error("Invalid URL: ${e.message}"))
            return
        }

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post {
                    completion(null, Error("Failed to obtain signature: ${e.message}"))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        mainHandler.post {
                            completion(null, Error("Unexpected response code: ${it.code}"))
                        }
                        return
                    }

                    val body = it.body?.string()
                    if (body != null) {
                        try {
                            val signatureResponse = gson.fromJson(body, SignatureResponse::class.java)
                            mainHandler.post {
                                if (signatureResponse.signature != null) {
                                    completion(signatureResponse.signature, null)
                                } else {
                                    completion(null, Error("Unexpected response: $body"))
                                }
                            }
                        } catch (ex: Exception) {
                            mainHandler.post {
                                completion(null, Error("Failed to parse JSON: ${ex.message}"))
                            }
                        }
                    } else {
                        mainHandler.post {
                            completion(null, Error("Response body is null"))
                        }
                    }
                }
            }
        })
    }
}

data class DydxMoonPayConfig(
    val isSandbox: Boolean,
    val moonPayPk: String,
    val moonPaySk: String?,
    val moonPaySignUrl: String?,
    val isDarkTheme: Boolean,
)

@Serializable
data class SignatureResponse(
    @SerializedName("signature") val signature: String?
)
