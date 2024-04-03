package exchange.dydx.trading.integration.cosmos

import exchange.dydx.integration.javascript.JavascriptApi
import exchange.dydx.trading.common.DydxException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber

private const val TAG = "CosmosV4Client"

interface CosmosV4WebviewClientProtocol : JavascriptApi, CosmosV4ClientProtocol

interface CosmosV4ClientProtocol {

    val initialized: StateFlow<Boolean>

    fun deriveCosmosKey(
        signature: String,
        completion: JavascriptCompletion,
    )

    fun connectNetwork(
        paramsInJson: String,
        completion: JavascriptCompletion,
    )

    fun connectWallet(
        mnemonic: String,
        completion: JavascriptCompletion,
    )

    fun call(
        functionName: String,
        paramsInJson: String?,
        completion: JavascriptCompletion,
    )

    fun withdrawToIBC(
        subaccount: Int,
        amount: String,
        payload: String,
        completion: JavascriptCompletion
    )

    fun encodeAccountRequestData(
        address: String,
        completion: JavascriptCompletion,
    )

    fun decodeAccountResponseValue(
        encodedAccountResponse: String,
        completion: JavascriptCompletion,
    )

    fun signPlaceOrderTransaction(
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
    )

    fun signCancelOrderTransaction(
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
    )

    fun echo(value: String, completion: CosmosV4ClientResponseHandler)
}

@kotlinx.serialization.Serializable
data class CosmosV4ClientResponse(
    val status: String = "UNDEFINED",
    val result: String? = null,
    val error: CosmosV4ClientError? = null,
)

@kotlinx.serialization.Serializable
data class CosmosV4ClientError(
    val name: String = "UNDEFINED",
    val message: String? = null,
    val stack: String? = null,
) {
    fun throwable(): Throwable {
        return CosmosV4ClientException("$name:$message", stack?.let { DydxException(it) })
    }
}
class CosmosV4ClientException(s: String, cause: Throwable? = null) : DydxException(s, cause)

typealias JavascriptCompletion = (result: String?) -> Unit

abstract class CosmosV4ClientResponseHandler(val json: Json) {
    fun completion(): JavascriptCompletion {
        return { result: String? ->
            if (result == null) {
                onError(null)
            } else {
                val response: CosmosV4ClientResponse = json.decodeFromString(result)
                when (response.status) {
                    "SUCCESS" -> onSuccess(response.result)
                    "ERROR" -> onError(response.error)
                    else -> {
                        Timber.tag(TAG).e("Unknown response status: %s", response.status)
                        onError(response.error)
                    }
                }
            }
        }
    }

    abstract fun onSuccess(result: String?)
    abstract fun onError(err: CosmosV4ClientError?)
}
