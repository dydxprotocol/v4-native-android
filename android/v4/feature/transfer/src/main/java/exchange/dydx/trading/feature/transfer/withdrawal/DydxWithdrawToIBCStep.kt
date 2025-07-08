package exchange.dydx.trading.feature.transfer.withdrawal

import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.utilities.utils.jsonStringToMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DydxWithdrawToIBCStep(
    private val transferInput: TransferInput,
    private val selectedSubaccount: Subaccount,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val parser: ParserProtocol,
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : AsyncStep<String> {

    override suspend fun run(): Result<String> {
        val amount = transferInput.size?.usdcSize ?: return invalidInputEvent
        val amountDecimal = parser.asDouble(amount) ?: 0.0
        if (amountDecimal <= 0.0) {
            return invalidInputEvent
        }
        val data = transferInput.requestPayload?.data ?: return invalidInputEvent

        return if (transferInput.isCctp) {
            suspendCoroutine { continuation ->
                abacusStateManager.commitCCTPWithdraw { success, parsingError, data ->
                    if (success) {
                        val response = data as? String
                        if (response != null) {
                            postTransaction(response, continuation)
                        } else {
                            continuation.resume(errorEvent(localizer.localize("APP.GENERAL.UNKNOWN_ERROR")))
                        }
                    } else {
                        continuation.resume(errorEvent(parsingError?.message ?: localizer.localize("APP.GENERAL.UNKNOWN_ERROR")))
                    }
                }
            }
        } else {
            suspendCoroutine { continuation ->
                cosmosClient.withdrawToIBC(
                    subaccount = selectedSubaccount.subaccountNumber,
                    amount = amount,
                    payload = data,
                    completion = { response ->
                        postTransaction(response, continuation)
                    },
                )
            }
        }
    }

    private fun postTransaction(response: String?, continuation: Continuation<Result<String>>) {
        val result = response?.jsonStringToMap()
        if (result == null) {
            continuation.resume(errorEvent(localizer.localize("APP.GENERAL.UNKNOWN_ERROR")))
            return
        }

        val error = result["error"] as? Map<String, Any>
        val transactionHash = parser.asString(result["transactionHash"])
        val hash = parser.asString(result["hash"])
        if (error != null) {
            continuation.resume(errorEvent(error["message"] as? String ?: "Unknown error"))
        } else if (transactionHash != null) {
            continuation.resume(Result.success("0x$transactionHash"))
        } else if (hash != null) {
            continuation.resume(Result.success("0x$hash"))
        } else {
            continuation.resume(errorEvent(localizer.localize("APP.V4.NO_HASH")))
        }
    }
}
