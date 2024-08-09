package exchange.dydx.trading.feature.transfer.deposit

import android.content.Context
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.cartera.walletprovider.EthereumTransactionRequest
import exchange.dydx.dydxCartera.steps.WalletSendTransactionStep
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.web3.ABIEncoder
import exchange.dydx.web3.EthereumInteractor
import exchange.dydx.web3.steps.EthEstimateGasStep
import exchange.dydx.web3.steps.EthGetGasPriceStep
import exchange.dydx.web3.steps.EthGetNonceStep
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
            ).run()
        }
        val gasEstimateAsync = async {
            EthEstimateGasStep(
                ethereumInteractor = ethereumInteractor,
            ).run()
        }
        val nonceAsync = async {
            EthGetNonceStep(
                address = ethereumAddress,
                ethereumInteractor = ethereumInteractor,
            ).run()
        }

        val gasPrice = gasPriceAsync.await().getOrNull() ?: return@coroutineScope errorEvent("gasPrice is null")
        val gasEstimate = gasEstimateAsync.await().getOrNull()
        val nonce = nonceAsync.await().getOrNull() ?: return@coroutineScope errorEvent("nonce is null")

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
            nonce = nonce.toInt(),
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
        ).run().map { true }
    }
}
