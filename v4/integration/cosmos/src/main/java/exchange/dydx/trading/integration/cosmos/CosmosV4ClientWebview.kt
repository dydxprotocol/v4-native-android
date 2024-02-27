package exchange.dydx.trading.integration.cosmos

import android.content.Context
import exchange.dydx.integration.javascript.JavascriptApiImpl
import exchange.dydx.integration.javascript.JavascriptRunnerV4
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.Locale

class CosmosV4ClientWebview(
    context: Context,
    filename: String = "v4-native-client.js",
) : CosmosV4WebviewClientProtocol,
    JavascriptApiImpl(
        context = context,
        description = filename,
        runner = JavascriptRunnerV4.runnerFromFile(context, filename)
            ?: throw IOException("Fatal, unable to load runner from: $filename"),
    ) {

    override val initialized = runner.initialized

    override fun deriveCosmosKey(
        signature: String,
        completion: JavascriptCompletion,
    ) {
        callNativeClient(
            "deriveMnemomicFromEthereumSignature",
            listOf(signature),
            completion,
        )
    }

    override fun connectNetwork(
        paramsInJson: String,
        completion: JavascriptCompletion,
    ) {
        callNativeClient(
            "connectNetwork",
            listOf(paramsInJson),
            completion,
        )
    }

    override fun connectWallet(
        mnemonic: String,
        completion: JavascriptCompletion,
    ) {
        callNativeClient(
            "connectWallet",
            listOf(mnemonic),
            completion,
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
            functionName,
            params,
            completion,
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
            "withdrawToIBC",
            listOf(subaccount, amount, base64String),
            completion,
        )
    }

    override fun encodeAccountRequestData(
        address: String,
        completion: JavascriptCompletion,
    ) {
        callNativeClient(
            "encodeAccountRequestData",
            listOf(address),
            completion,
        )
    }

    override fun decodeAccountResponseValue(
        encodedAccountResponse: String,
        completion: JavascriptCompletion,
    ) {
        callNativeClient(
            "decodeAccountResponseValue",
            listOf(encodedAccountResponse),
            completion,
        )
    }

    override fun signPlaceOrderTransaction(
        chainId: String,
        address: String,
        mnemonic: String,
        accountNumber: Int,
        sequence: Int,
        subaccountNumber: Int,
        clobPairId: Int,
        side: Int,
        quantums: Double,
        subticks: Double,
        goodTilBlock: Int?,
        goodTilTime: Int?,
        clientId: Int,
        timeInForce: Int,
        orderFlags: Int,
        reduceOnly: Boolean,
        completion: CosmosV4ClientResponseHandler,
    ) {
        callNativeClient(
            "signPlaceOrderWithCallback",
            listOf(
                chainId,
                address,
                mnemonic,
                accountNumber,
                sequence,
                subaccountNumber,
                clobPairId,
                side,
                quantums,
                subticks,
                goodTilBlock ?: 0,
                goodTilTime ?: 0,
                clientId,
                timeInForce,
                orderFlags,
                reduceOnly,
            ),
            completion.completion(),
        )
    }

    override fun signCancelOrderTransaction(
        chainId: String,
        address: String,
        mnemonic: String,
        accountNumber: Int,
        sequence: Int,
        subaccountNumber: Int,
        clobPairId: Int,
        clientId: Int,
        goodTilBlock: Int?,
        completion: CosmosV4ClientResponseHandler,
    ) {
        callNativeClient(
            "signCancelOrderWithCallback",
            listOf(
                chainId,
                address,
                mnemonic,
                accountNumber,
                sequence,
                subaccountNumber,
                clobPairId,
                clientId,
                goodTilBlock ?: 0,
            ),
            completion.completion(),
        )
    }

    private fun callNativeClient(
        functionName: String,
        params: List<Any>,
        completion: JavascriptCompletion,
    ) {
        runBlocking {
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
                completion(result?.response)
            }
        }
    }

    override fun echo(value: String, completion: CosmosV4ClientResponseHandler) {
//        completion.onSuccess(value)
        callNativeClient(
            "echoCallback",
            listOf(value),
            completion.completion(),
        )
    }
}
