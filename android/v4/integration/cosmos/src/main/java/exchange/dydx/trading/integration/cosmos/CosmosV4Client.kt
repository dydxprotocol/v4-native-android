package exchange.dydx.trading.integration.cosmos

import exchange.dydx.integration.javascript.JavascriptApi
import exchange.dydx.trading.common.DydxException
import kotlinx.coroutines.flow.StateFlow

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

    fun depositToMegavault(
        subaccountNumber: Int,
        amountUsdc: Double,
        completion: JavascriptCompletion
    )

    fun withdrawFromMegavault(
        subaccountNumber: Int,
        shares: Long,
        minAmount: Long,
        completion: JavascriptCompletion
    )

    fun getMegavaultWithdrawalInfo(
        shares: Long,
        completion: JavascriptCompletion
    )
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
