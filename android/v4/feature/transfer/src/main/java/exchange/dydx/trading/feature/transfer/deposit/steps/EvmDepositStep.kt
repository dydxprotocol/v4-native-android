package exchange.dydx.trading.feature.transfer.deposit.steps

import android.content.Context
import exchange.dydx.abacus.output.input.TransferInputRequestPayload
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.cartera.walletprovider.EthereumTransactionRequest
import exchange.dydx.dydxCartera.steps.WalletSendTransactionStep
import exchange.dydx.trading.feature.transfer.tokenSize
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.utilities.utils.runWithLogs
import java.math.BigInteger
import kotlin.io.encoding.ExperimentalEncodingApi

class EvmDepositStep(
    private val provider: CarteraProvider,
    private val walletAddress: String,
    private val walletId: String?,
    private val chainRpc: String?,
    private val tokenAddress: String,
    private val context: Context,
    private val requestPayload: TransferInputRequestPayload,
    private val chainId: String,
    private val tokenSize: BigInteger,
) : AsyncStep<String> {

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun run(): Result<String> {
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
        val ethereum = EthereumTransactionRequest(
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

        return WalletSendTransactionStep(
            ethereum = ethereum,
            solana = null,
            chainId = chainId,
            walletAddress = walletAddress,
            walletId = walletId,
            context = context,
            provider = provider,
        ).runWithLogs()
    }
}
