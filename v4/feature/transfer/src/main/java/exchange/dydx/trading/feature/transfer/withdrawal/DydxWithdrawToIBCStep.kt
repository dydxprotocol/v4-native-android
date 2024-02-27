package exchange.dydx.trading.feature.transfer.withdrawal

import exchange.dydx.abacus.output.Subaccount
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.AsyncEvent
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.utilities.utils.jsonStringToMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class DydxWithdrawToIBCStep(
    private val transferInput: TransferInput,
    private val selectedSubaccount: Subaccount,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val parser: ParserProtocol,
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : AsyncStep<Unit, String> {

    private val eventFlow: MutableStateFlow<AsyncEvent<Unit, String>> =
        MutableStateFlow(AsyncEvent.Progress(Unit))

    override fun run(): Flow<AsyncEvent<Unit, String>> {
        val amount = transferInput.size?.usdcSize ?: return flowOf(invalidInputEvent)
        val amountDecimal = parser.asDouble(amount) ?: 0.0
        if (amountDecimal <= 0.0) {
            return flowOf(invalidInputEvent)
        }
        val data = transferInput.requestPayload?.data ?: return flowOf(invalidInputEvent)

        if (transferInput.isCctp) {
            abacusStateManager.commitCCTPWithdraw { success, parsingError, data ->
                if (success) {
                    val response = data as? String
                    if (response != null) {
                        postTransaction(response)
                    } else {
                        eventFlow.value = errorEvent(localizer.localize("APP.GENERAL.UNKNOWN_ERROR"))
                    }
                } else {
                    eventFlow.value = errorEvent(parsingError?.message ?: localizer.localize("APP.GENERAL.UNKNOWN_ERROR"))
                }
            }
        } else {
            cosmosClient.withdrawToIBC(
                subaccount = selectedSubaccount.subaccountNumber,
                amount = amount,
                payload = data,
                completion = { response ->
                    postTransaction(response)
                },
            )
        }

        return eventFlow
    }

    private fun postTransaction(response: String?) {
        val result = response?.jsonStringToMap()
        if (result == null) {
            eventFlow.value = errorEvent(localizer.localize("APP.GENERAL.UNKNOWN_ERROR"))
            return
        }

        val error = result["error"] as? Map<String, Any>
        val transactionHash = parser.asString(result["transactionHash"])
        val hash = parser.asString(result["hash"])
        if (error != null) {
            eventFlow.value = errorEvent(error["message"] as? String ?: "Unknown error")
        } else if (transactionHash != null) {
            eventFlow.value =
                AsyncEvent.Result(result = "0x$transactionHash", error = null)
        } else if (hash != null) {
            eventFlow.value = AsyncEvent.Result(result = "0x$hash", error = null)
        } else {
            eventFlow.value = errorEvent(localizer.localize("APP.V4.NO_HASH"))
        }
    }
}
