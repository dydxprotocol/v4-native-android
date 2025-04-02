package exchange.dydx.trading.feature.transfer.deposit

import android.R.attr.value
import android.content.Context
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.cartera.walletprovider.EthereumTransactionRequest
import exchange.dydx.dydxCartera.steps.WalletSendTransactionStep
import exchange.dydx.trading.feature.shared.TransferTokenDetails
import exchange.dydx.trading.feature.transfer.tokenSize
import exchange.dydx.trading.feature.transfer.utils.TransferRouteSelection
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.utilities.utils.runWithLogs
import org.bouncycastle.crypto.params.Blake3Parameters.context
import kotlin.io.encoding.Base64
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
        val tokenSize = transferInput.tokenSize(transferTokenDetails) ?: return invalidInputEvent
        val chainId = transferInput.chain ?: return invalidInputEvent

        val ethereum: EthereumTransactionRequest?
        val solana: ByteArray?

        if (chainId == "solana" || chainId == "solana-devnet") {
            val base64Payload = requestPayload.data
            if (base64Payload == null) {
                return errorEvent("Invalid base64 payload")
            }
            solana = Base64.decode(base64Payload)
            ethereum = null
        } else {
            if (chainRpc == null) {
                return errorEvent("Invalid chain RPC")
            }
            val value = requestPayload.value ?: return errorEvent("Invalid value")
            val targetAddress = requestPayload.targetAddress ?: return errorEvent("Invalid target address")

            val approveERC20Result = EnableERC20TokenStep(
                chainRpc = chainRpc,
                tokenAddress = tokenAddress,
                ethereumAddress = walletAddress,
                spenderAddress = targetAddress,
                desiredAmount = tokenSize,
                walletId = walletId,
                chainId = chainId,
                provider = provider,
                context = context,
            ).runWithLogs()

            val approved = approveERC20Result.getOrNull()
            if (approveERC20Result.isFailure || approved == false) {
                return errorEvent(
                    approveERC20Result.exceptionOrNull()?.message ?: "Token not enabled",
                )
            }

            ethereum = EthereumTransactionRequest(
                fromAddress = walletAddress,
                toAddress = targetAddress,
                weiValue = value.toBigInteger(),
                data = requestPayload.data ?: "0x0",
                nonce = null,
                gasPriceInWei = requestPayload.gasPrice?.toBigInteger(),
                maxFeePerGas = requestPayload.maxFeePerGas?.toBigInteger(),
                maxPriorityFeePerGas = requestPayload.maxPriorityFeePerGas?.toBigInteger(),
                gasLimit = requestPayload.gasLimit?.toBigInteger(),
                chainId = chainId,
            )
            solana = null
        }

        return WalletSendTransactionStep(
            ethereum = ethereum,
            solana = solana,
            chainId = chainId,
            walletAddress = walletAddress,
            walletId = walletId,
            context = context,
            provider = provider,
        ).runWithLogs()
    }
}
