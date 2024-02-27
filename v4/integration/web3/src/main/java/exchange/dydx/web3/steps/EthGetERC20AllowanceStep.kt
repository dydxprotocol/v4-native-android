package exchange.dydx.web3.steps

import exchange.dydx.utilities.utils.AsyncEvent
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.web3.EthereumInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigInteger

class EthGetERC20AllowanceStep(
    private val ethereumInteractor: EthereumInteractor,
    private val tokenAddress: String,
    private val ethereumAddress: String,
    private val spenderAddress: String,
) : AsyncStep<Unit, BigInteger> {
    private val eventFlow: MutableStateFlow<AsyncEvent<Unit, BigInteger>> = MutableStateFlow(AsyncEvent.Progress(Unit))

    override fun run(): Flow<AsyncEvent<Unit, BigInteger>> {
        ethereumInteractor.erc20GetAllowance(
            tokenAddress = tokenAddress,
            ownerAddress = ethereumAddress,
            spenderAddress = spenderAddress,
            completion = { error, allowance ->
                if (allowance != null) {
                    eventFlow.value = AsyncEvent.Result(result = allowance, error = null)
                } else {
                    eventFlow.value = errorEvent(error?.message ?: "Unknown error")
                }
            },
        )

        return eventFlow
    }
}
