package exchange.dydx.trading.feature.transfer.deposit

import android.content.Context
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.cartera.walletprovider.EthereumTransactionRequest
import exchange.dydx.dydxCartera.steps.WalletSendTransactionStep
import exchange.dydx.utilities.utils.AsyncEvent
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.web3.EthereumInteractor
import exchange.dydx.web3.steps.EthGetNonceStep
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
) : AsyncStep<Unit, String> {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun run(): Flow<AsyncEvent<Unit, String>> {
        val requestPayload = transferInput.requestPayload ?: return flowOf(invalidInputEvent)
        val targetAddress = requestPayload.targetAddress ?: return flowOf(invalidInputEvent)
        val tokenSize = transferInput.tokenSize ?: return flowOf(invalidInputEvent)
        val walletId = walletId ?: return flowOf(invalidInputEvent)
        val chainId = transferInput.chain ?: return flowOf(invalidInputEvent)
        val value = requestPayload.value ?: return flowOf(invalidInputEvent)

        return EnableERC20TokenStep(
            chainRpc = chainRpc,
            tokenAddress = tokenAddress,
            ethereumAddress = walletAddress,
            spenderAddress = targetAddress,
            desiredAmount = tokenSize,
            walletId = walletId,
            chainId = chainId,
            provider = provider,
            context = context,
        ).run()
            .flatMapLatest { event ->
                val eventResult =
                    event as? AsyncEvent.Result ?: return@flatMapLatest flowOf()
                val approved = eventResult.result
                val error = eventResult.error
                if (error != null || approved == false) {
                    return@flatMapLatest flowOf(errorEvent(error?.message ?: "Token not enabled"))
                }

                EthGetNonceStep(
                    address = walletAddress,
                    ethereumInteractor = EthereumInteractor(chainRpc),
                ).run()
            }
            .flatMapLatest { event ->
                val eventResult =
                    event as? AsyncEvent.Result ?: return@flatMapLatest flowOf()
                val nonce = eventResult.result as? BigInteger
                val error = eventResult.error
                if (error != null || nonce == null) {
                    return@flatMapLatest flowOf(errorEvent(error?.message ?: "Invalid Nonce"))
                }

                val transaction = EthereumTransactionRequest(
                    fromAddress = walletAddress,
                    toAddress = targetAddress,
                    weiValue = value.toBigInteger(),
                    data = requestPayload.data ?: "0x0",
                    nonce = nonce?.toInt(),
                    gasPriceInWei = requestPayload.gasPrice?.toBigInteger(),
                    maxFeePerGas = requestPayload.maxFeePerGas?.toBigInteger(),
                    maxPriorityFeePerGas = requestPayload.maxPriorityFeePerGas?.toBigInteger(),
                    gasLimit = requestPayload.gasLimit?.toBigInteger(),
                    chainId = chainId,
                )
                WalletSendTransactionStep(
                    transaction = transaction,
                    chainId = chainId,
                    walletAddress = walletAddress,
                    walletId = walletId,
                    context = context,
                    provider = provider,
                ).run()
            }
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
