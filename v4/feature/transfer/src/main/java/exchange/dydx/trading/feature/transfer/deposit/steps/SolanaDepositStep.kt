package exchange.dydx.trading.feature.transfer.deposit.steps

import android.content.Context
import exchange.dydx.abacus.output.input.TransferInputRequestPayload
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.dydxCartera.steps.WalletSendTransactionStep
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.utilities.utils.runWithLogs
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class SolanaDepositStep(
    private val provider: CarteraProvider,
    private val walletAddress: String,
    private val walletId: String?,
    private val context: Context,
    private val requestPayload: TransferInputRequestPayload,
    private val isMainnet: Boolean,
) : AsyncStep<String> {

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun run(): Result<String> {
        val base64Payload = requestPayload.data
        if (base64Payload == null) {
            return errorEvent("Invalid base64 payload")
        }
        val solana = Base64.Default.decode(base64Payload)

        return WalletSendTransactionStep(
            ethereum = null,
            solana = solana,
            chainId = if (isMainnet) {
                "1"
            } else {
                "2"
            },
            walletAddress = walletAddress,
            walletId = walletId,
            context = context,
            provider = provider,
        ).runWithLogs()
    }
}
