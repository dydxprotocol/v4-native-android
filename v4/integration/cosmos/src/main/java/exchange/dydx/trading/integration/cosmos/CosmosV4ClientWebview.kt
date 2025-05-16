package exchange.dydx.trading.integration.cosmos

import android.app.Application
import exchange.dydx.integration.javascript.JavascriptApiImpl
import exchange.dydx.integration.javascript.JavascriptRunnerV4
import exchange.dydx.trading.common.BuildConfig
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val WEBVIEW_FILENAME = "v4-native-client.js"

private const val TAG = "CosmosV4ClientWebview"

@Singleton
class CosmosV4ClientWebview @Inject constructor(
    application: Application,
    @CoroutineScopes.App appScope: CoroutineScope,
    private val logger: Logging,
    private val tracker: Tracking,
) : CosmosV4WebviewClientProtocol,
    JavascriptApiImpl(
        context = application,
        description = WEBVIEW_FILENAME,
        runner = JavascriptRunnerV4.runnerFromFile(appScope, application, WEBVIEW_FILENAME, logger)
            ?: throw IOException("Fatal, unable to load runner from: $WEBVIEW_FILENAME"),
    ) {

    override val initialized = runner.initialized

    private var connectNewtworkParams: String? = null
    private var connectWalletParams: String? = null

    override fun deriveCosmosKey(
        signature: String,
        completion: JavascriptCompletion,
    ) {
        callNativeClient(
            functionName = "deriveMnemomicFromEthereumSignature",
            params = listOf(signature),
            completion = completion,
        )
    }

    override fun connectNetwork(
        paramsInJson: String,
        completion: JavascriptCompletion,
    ) {
        connectNewtworkParams = paramsInJson
        callNativeClient(
            functionName = "connectNetwork",
            params = listOf(paramsInJson),
            completion = completion,
        )
    }

    override fun connectWallet(
        mnemonic: String,
        completion: JavascriptCompletion,
    ) {
        connectWalletParams = mnemonic
        callNativeClient(
            functionName = "connectWallet",
            params = listOf(mnemonic),
            completion = completion,
        )
    }

    override fun call(
        functionName: String,
        paramsInJson: String?,
        completion: JavascriptCompletion,
    ) {
        reconnectIfNeeded(functionName) {
            val params = if (paramsInJson != null) {
                listOf(paramsInJson)
            } else {
                listOf()
            }
            callNativeClient(
                functionName = functionName,
                params = params,
                completion = completion,
            )
        }
    }

    override fun withdrawToIBC(
        subaccount: Int,
        amount: String,
        payload: String,
        completion: JavascriptCompletion
    ) {
        reconnectIfNeeded("withdrawToIBC") {
            val data = payload.toByteArray()
            val base64String = android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP)
            callNativeClient(
                functionName = "withdrawToIBC",
                params = listOf(subaccount, amount, base64String),
                completion = completion,
            )
        }
    }

    override fun depositToMegavault(
        subaccountNumber: Int,
        amountUsdc: Double,
        completion: JavascriptCompletion
    ) {
        reconnectIfNeeded("depositToMegavault") {
            callNativeClient(
                functionName = "depositToMegavault",
                params = listOf(subaccountNumber, amountUsdc),
                completion = completion,
            )
        }
    }

    override fun withdrawFromMegavault(
        subaccountNumber: Int,
        shares: Long,
        minAmount: Long,
        completion: JavascriptCompletion
    ) {
        reconnectIfNeeded("withdrawFromMegavault") {
            callNativeClient(
                functionName = "withdrawFromMegavault",
                params = listOf(subaccountNumber, shares, minAmount),
                completion = completion,
            )
        }
    }

    override fun getMegavaultWithdrawalInfo(
        shares: Long,
        completion: JavascriptCompletion
    ) {
        reconnectIfNeeded("getMegavaultWithdrawalInfo") {
            callNativeClient(
                functionName = "getMegavaultWithdrawalInfo",
                params = listOf(shares),
                completion = completion,
            )
        }
    }

    private fun reconnectIfNeeded(functionName: String, completion: JavascriptCompletion) {
        callNativeClient("isWalletConnected", listOf()) { payload ->
            var shouldReset: Boolean
            if (payload == null) {
                shouldReset = true
            } else {
                try {
                    val json = Json.parseToJsonElement(payload)
                    val result = json.jsonObject["result"]
                    shouldReset = result.toString() != "true"
                } catch (e: Exception) {
                    shouldReset = true
                }
            }

            if (shouldReset) {
                tracker.log("AndroidV4ClientReconnect", mapOf("functionName" to functionName))
                val connectWalletParams = connectWalletParams
                val connectNewtworkParams = connectNewtworkParams
                if (connectNewtworkParams != null) {
                    connectNetwork(connectNewtworkParams) {
                        if (connectWalletParams != null) {
                            connectWallet(connectWalletParams) {
                                completion(it)
                            }
                        } else {
                            completion(it)
                        }
                    }
                } else {
                    completion(null)
                }
            } else {
                completion(null)
            }
        }
    }

    private fun callNativeClient(
        functionName: String,
        params: List<Any>,
        completion: JavascriptCompletion,
    ) {
        val jsParams = params
            .map {
                when (it) {
                    is String -> "'$it'"
                    is Double -> String.format(Locale.ROOT, "%.6f", it)
                    else -> it.toString()
                }
            }
        runner.runJs(
            function = functionName,
            params = jsParams,
        ) { result ->
            // for debug builds, log the full params, otherwise redact them
            val paramsString = if (BuildConfig.DEBUG) "$params" else "[REDUCTED]"
            logger.d(TAG, "callNativeClient $functionName, params: $paramsString, result: $result")
            completion(result?.response)
        }
    }
}
