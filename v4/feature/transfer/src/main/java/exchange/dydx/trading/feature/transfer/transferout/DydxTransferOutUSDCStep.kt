package exchange.dydx.trading.feature.transfer.transferout

import exchange.dydx.abacus.output.Subaccount
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.toJson
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.AsyncEvent
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.utilities.utils.jsonStringToMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class DydxTransferOutUSDCStep(
    private val transferInput: TransferInput,
    private val selectedSubaccount: Subaccount,
    private val usdcTokenAmount: Double?,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val parser: ParserProtocol,
    private val localizer: LocalizerProtocol
) : AsyncStep<Unit, String> {

    private val eventFlow: MutableStateFlow<AsyncEvent<Unit, String>> =
        MutableStateFlow(AsyncEvent.Progress(Unit))

    override fun run(): Flow<AsyncEvent<Unit, String>> {
        val amount = transferInput.size?.usdcSize ?: return flowOf(invalidInputEvent)
        val amountDecimal = parser.asDouble(amount) ?: 0.0
        if (amountDecimal <= 0.0) {
            return flowOf(invalidInputEvent)
        }
        val subaccountNumber = selectedSubaccount.subaccountNumber
        val gasFee = transferInput.summary?.gasFee ?: 0.0
        val usdcBalanceInWallet = usdcTokenAmount ?: 0.0
        val recipient = transferInput.address ?: return flowOf(invalidInputEvent)

        if (usdcBalanceInWallet > gasFee) {
            val payload: Map<String, Any> = mapOf(
                "subaccountNumber" to subaccountNumber,
                "amount" to amount,
                "recipient" to recipient,
            )
            val paramsInJson = payload.toJson()
            cosmosClient.call(
                functionName = "withdraw",
                paramsInJson = paramsInJson,
                completion = { response ->
                    val result = response?.jsonStringToMap() ?: return@call
                    val error = result["error"] as? Map<String, Any>
                    val transactionHash = parser.asString(result["transactionHash"])
                    val hash = parser.asString(result["hash"])
                    if (error != null) {
                        eventFlow.value = errorEvent(error["message"] as? String ?: "Unknown error")
                    } else if (transactionHash != null) {
                        eventFlow.value = AsyncEvent.Result(result = "0x$transactionHash", error = null)
                    } else if (hash != null) {
                        eventFlow.value = AsyncEvent.Result(result = "0x$hash", error = null)
                    } else {
                        eventFlow.value = errorEvent(localizer.localize("APP.V4.NO_HASH"))
                    }
                },
            )
        } else {
            eventFlow.value = errorEvent(localizer.localize("APP.V4.NO_GAS_BODY"))
        }

        return eventFlow
    }
}
