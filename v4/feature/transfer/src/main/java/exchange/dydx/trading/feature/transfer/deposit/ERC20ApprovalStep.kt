package exchange.dydx.trading.feature.transfer.deposit

import android.content.Context
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.cartera.walletprovider.EthereumTransactionRequest
import exchange.dydx.dydxCartera.steps.WalletSendTransactionStep
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.utilities.utils.runWithLogs
import exchange.dydx.web3.ABIEncoder
import exchange.dydx.web3.EthereumInteractor
import exchange.dydx.web3.steps.EthEstimateGasStep
import exchange.dydx.web3.steps.EthGetGasPriceStep
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.math.BigInteger

class ERC20ApprovalStep(
    private val chainRpc: String,
    private val tokenAddress: String,
    private val ethereumAddress: String,
    private val spenderAddress: String,
    private val desiredAmount: BigInteger?,
    private val walletId: String?,
    private val chainId: String,
    private val provider: CarteraProvider,
    private val context: Context,
) : AsyncStep<Boolean> {

    private val ethereumInteractor = EthereumInteractor(chainRpc)

    override suspend fun run(): Result<Boolean> = coroutineScope {
        val gasPriceAsync = async {
            EthGetGasPriceStep(
                ethereumInteractor = ethereumInteractor,
            ).runWithLogs()
        }
        val gasEstimateAsync = async {
            EthEstimateGasStep(
                ethereumInteractor = ethereumInteractor,
            ).runWithLogs()
        }

        val gasPrice = gasPriceAsync.await().getOrNull() ?: return@coroutineScope errorEvent("gasPrice is null")
        val gasEstimate = gasEstimateAsync.await().getOrNull()

        val function = if (desiredAmount != null) {
            ABIEncoder.encodeERC20ApproveFunction(
                spenderAddress = spenderAddress,
                desiredAmount = desiredAmount,
            )
        } else {
            ABIEncoder.encodeERC20ApproveFunction(
                spenderAddress = spenderAddress,
            )
        }

        val transaction = EthereumTransactionRequest(
            fromAddress = ethereumAddress,
            toAddress = tokenAddress,
            weiValue = BigInteger.valueOf(0),
            data = function,
            nonce = null,
            gasPriceInWei = gasPrice,
            maxFeePerGas = null,
            maxPriorityFeePerGas = null,
            gasLimit = gasEstimate,
            chainId = chainId,
        )

        return@coroutineScope WalletSendTransactionStep(
            transaction = transaction,
            chainId = chainId,
            walletAddress = ethereumAddress,
            walletId = walletId,
            context = context,
            provider = provider,
        ).runWithLogs().map { true }
    }
}
