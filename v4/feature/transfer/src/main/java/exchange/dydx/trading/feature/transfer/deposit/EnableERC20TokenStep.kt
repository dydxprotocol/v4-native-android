package exchange.dydx.trading.feature.transfer.deposit

import android.content.Context
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.utilities.utils.AsyncEvent
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.web3.EthereumInteractor
import exchange.dydx.web3.steps.EthGetERC20AllowanceStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.math.BigInteger

class EnableERC20TokenStep(
    private val chainRpc: String,
    private val tokenAddress: String,
    private val ethereumAddress: String,
    private val spenderAddress: String,
    private val desiredAmount: BigInteger,
    private val walletId: String?,
    private val chainId: String,
    private val provider: CarteraProvider,
    private val context: Context,
) : AsyncStep<Unit, Boolean> {

    override fun run(): Flow<AsyncEvent<Unit, Boolean>> {
        if (tokenAddress == "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE") {
            return flowOf(AsyncEvent.Result(result = true, error = null))
        }

        return EthGetERC20AllowanceStep(
            ethereumInteractor = EthereumInteractor(chainRpc),
            tokenAddress = tokenAddress,
            ethereumAddress = ethereumAddress,
            spenderAddress = spenderAddress,
        ).run()
            .filter { it.isResult }
            .flatMapLatest { event ->
                val eventResult = event as? AsyncEvent.Result ?: return@flatMapLatest flowOf()
                val allowance = eventResult.result
                val error = eventResult.error
                if (allowance != null) {
                    if (allowance >= desiredAmount) {
                        return@flatMapLatest flowOf(
                            AsyncEvent.Result(
                                result = true,
                                error = null,
                            ),
                        )
                    } else {
                        ERC20ApprovalStep(
                            chainRpc = chainRpc,
                            tokenAddress = tokenAddress,
                            ethereumAddress = ethereumAddress,
                            spenderAddress = spenderAddress,
                            desiredAmount = desiredAmount,
                            walletId = walletId,
                            chainId = chainId,
                            provider = provider,
                            context = context,
                        ).run()
                    }
                } else {
                    return@flatMapLatest flowOf(
                        AsyncEvent.Result(
                            result = false,
                            error = error,
                        ),
                    )
                }
            }
    }
}
