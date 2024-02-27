package exchange.dydx.trading.feature.transfer.deposit

import android.content.Context
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.cartera.walletprovider.EthereumTransactionRequest
import exchange.dydx.dydxCartera.steps.WalletSendTransactionStep
import exchange.dydx.utilities.utils.AsyncEvent
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.web3.ABIEncoder
import exchange.dydx.web3.EthereumInteractor
import exchange.dydx.web3.steps.EthEstimateGasStep
import exchange.dydx.web3.steps.EthGetGasPriceStep
import exchange.dydx.web3.steps.EthGetNonceStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
) : AsyncStep<Unit, Boolean> {

    private val ethereumInteractor = EthereumInteractor(chainRpc)

    override fun run(): Flow<AsyncEvent<Unit, Boolean>> {
        return combine(
            EthGetGasPriceStep(
                ethereumInteractor = ethereumInteractor,
            ).run().filter { it.isResult },
            EthEstimateGasStep(
                ethereumInteractor = ethereumInteractor,
            ).run().filter { it.isResult },
            EthGetNonceStep(
                address = ethereumAddress,
                ethereumInteractor = ethereumInteractor,
            ).run().filter { it.isResult },
        ) { gasPriceEvent, gasEstimateEvent, nonceEvent ->
            val gasPrice = (gasPriceEvent as? AsyncEvent.Result)?.result
            val gasEstimate = (gasEstimateEvent as? AsyncEvent.Result)?.result
            val nonce = (nonceEvent as? AsyncEvent.Result)?.result
            if (gasPrice != null && gasEstimate != null && nonce != null) {
                return@combine Triple(gasPrice, gasEstimate, nonce)
            } else {
                return@combine null
            }
        }
            .filter { it != null }
            .flatMapLatest { triple ->
                val (gasPrice, gasEstimate, nonce) = triple ?: return@flatMapLatest flowOf()

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
                WalletSendTransactionStep(
                    transaction = transaction,
                    chainId = chainId,
                    walletAddress = ethereumAddress,
                    walletId = walletId,
                    context = context,
                    provider = provider,
                ).run()
            }
            .flatMapLatest { event ->
                val eventResult = event as? AsyncEvent.Result ?: return@flatMapLatest flowOf()
                val result = eventResult.result
                val error = eventResult.error
                if (result != null) {
                    return@flatMapLatest flowOf(AsyncEvent.Result(result = true, error = null))
                } else {
                    return@flatMapLatest flowOf(AsyncEvent.Result(result = false, error = error))
                }
            }
    }
}
