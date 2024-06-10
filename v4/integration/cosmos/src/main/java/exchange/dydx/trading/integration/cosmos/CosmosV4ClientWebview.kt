package exchange.dydx.trading.integration.cosmos

import android.app.Application
import exchange.dydx.integration.javascript.JavascriptApiImpl
import exchange.dydx.integration.javascript.JavascriptRunnerV4
import exchange.dydx.trading.common.BuildConfig
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.CoroutineScope
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
) : CosmosV4WebviewClientProtocol,
    JavascriptApiImpl(
        context = application,
        description = WEBVIEW_FILENAME,
        runner = JavascriptRunnerV4.runnerFromFile(appScope, application, WEBVIEW_FILENAME, logger)
            ?: throw IOException("Fatal, unable to load runner from: $WEBVIEW_FILENAME"),
    ) {

    override val initialized = runner.initialized

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

    override fun withdrawToIBC(
        subaccount: Int,
        amount: String,
        payload: String,
        completion: JavascriptCompletion
    ) {
        val data = payload.toByteArray()
        val base64String = android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP)
        callNativeClient(
            functionName = "withdrawToIBC",
            params = listOf(subaccount, amount, base64String),
            completion = completion,
        )
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
