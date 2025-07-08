package exchange.dydx.trading.feature.transfer.transferout

import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.filterNotNull
import exchange.dydx.abacus.utils.toJson
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.utilities.utils.jsonStringToMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DydxTransferOutDYDXStep(
    private val transferInput: TransferInput,
    private val nativeTokenAmount: Double?,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val parser: ParserProtocol,
    private val localizer: LocalizerProtocol
) : AsyncStep<String> {

    override suspend fun run(): Result<String> {
        val amount = transferInput.size?.size ?: return invalidInputEvent
        val amountDecimal = parser.asDouble(amount) ?: 0.0
        if (amountDecimal <= 0.0) {
            return invalidInputEvent
        }
        val gasFee = transferInput.summary?.gasFee ?: 0.0
        val nativeTokenBalanceInWallet = nativeTokenAmount ?: 0.0
        val recipient = transferInput.address ?: return invalidInputEvent
        val memo = transferInput.memo

        if (amountDecimal + gasFee <= nativeTokenBalanceInWallet) {
            val payload: Map<String, Any> = mapOf(
                "amount" to amount,
                "recipient" to recipient,
                "memo" to memo,
            )
                .filterNotNull()

            val paramsInJson = payload.toJson()
            return suspendCoroutine { continuation ->
                cosmosClient.call(
                    functionName = "transferNativeToken",
                    paramsInJson = paramsInJson,
                    completion = { response ->
                        val result = response?.jsonStringToMap() ?: return@call
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
                    },
                )
            }
        } else {
            return errorEvent(localizer.localize("APP.V4.NO_GAS_BODY"))
        }
    }
}
