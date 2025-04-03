package exchange.dydx.trading.feature.transfer.deposit.steps

import android.content.Context
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.trading.feature.shared.TransferTokenDetails
import exchange.dydx.trading.feature.transfer.tokenSize
import exchange.dydx.trading.feature.transfer.utils.TransferRouteSelection
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.utilities.utils.runWithLogs
import kotlin.io.encoding.ExperimentalEncodingApi

class DydxTransferDepositStep(
    private val transferInput: TransferInput,
    private val provider: CarteraProvider,
    private val walletAddress: String,
    private val walletId: String?,
    private val chainRpc: String?,
    private val tokenAddress: String,
    private val context: Context,
    private val selectedRoute: TransferRouteSelection,
    private val transferTokenDetails: TransferTokenDetails,
) : AsyncStep<String> {

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun run(): Result<String> {
        val requestPayload = if (selectedRoute == TransferRouteSelection.Instant) {
            transferInput.goFastRequestPayload
        } else {
            transferInput.requestPayload
        }
        if (requestPayload == null) {
            return errorEvent("Invalid request payload")
        }
        val chainId = transferInput.chain ?: return invalidInputEvent

        if (chainId == "solana" || chainId == "solana-devnet") {
            return SolanaDepositStep(
                provider = provider,
                walletAddress = walletAddress,
                walletId = walletId,
                context = context,
                requestPayload = requestPayload,
                isMainnet = chainId == "solana",
            ).runWithLogs()
        } else {
            return EvmDepositStep(
                provider = provider,
                walletAddress = walletAddress,
                walletId = walletId,
                chainRpc = chainRpc,
                tokenAddress = tokenAddress,
                context = context,
                requestPayload = requestPayload,
                chainId = chainId,
                tokenSize = transferInput.tokenSize(transferTokenDetails) ?: return invalidInputEvent,
            ).runWithLogs()
        }
    }
}
