package exchange.dydx.trading.feature.transfer.deposit

import android.content.Context
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.cartera.walletprovider.EthereumTransactionRequest
import exchange.dydx.dydxCartera.steps.WalletSendTransactionStep
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.utilities.utils.runWithLogs
import java.math.BigInteger
import kotlin.math.pow

class DydxTransferDepositStep(
    private val transferInput: TransferInput,
    private val provider: CarteraProvider,
    private val walletAddress: String,
    private val walletId: String?,
    private val chainRpc: String,
    private val tokenAddress: String,
    private val context: Context,
) : AsyncStep<String> {

    override suspend fun run(): Result<String> {
        val requestPayload = transferInput.requestPayload ?: return invalidInputEvent
        val targetAddress = requestPayload.targetAddress ?: return invalidInputEvent
        val tokenSize = transferInput.tokenSize ?: return invalidInputEvent
        val chainId = transferInput.chain ?: return invalidInputEvent
        val value = requestPayload.value ?: return invalidInputEvent

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
            return errorEvent(approveERC20Result.exceptionOrNull()?.message ?: "Token not enabled")
        }

        val transaction = EthereumTransactionRequest(
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
            transaction = transaction,
            chainId = chainId,
            walletAddress = walletAddress,
            walletId = walletId,
            context = context,
            provider = provider,
        ).runWithLogs()
    }
}

private val TransferInput.tokenSize: BigInteger?
    get() {
        val size = size?.size?.toDouble()
        val decimals = resources?.tokenResources?.get(token)?.decimals?.toDouble()
        if (size != null && decimals != null) {
            val intSize = size * 10.0.pow(decimals)
            return intSize.toBigDecimal().toBigInteger()
        }

        return null
    }
