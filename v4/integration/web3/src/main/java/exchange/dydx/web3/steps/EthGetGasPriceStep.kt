package exchange.dydx.web3.steps

import exchange.dydx.utilities.utils.AsyncEvent
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.web3.EthereumInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigInteger

class EthGetGasPriceStep(
    private val ethereumInteractor: EthereumInteractor,
) : AsyncStep<Unit, BigInteger> {
    private val eventFlow: MutableStateFlow<AsyncEvent<Unit, BigInteger>> = MutableStateFlow(AsyncEvent.Progress(Unit))

    override fun run(): Flow<AsyncEvent<Unit, BigInteger>> {
        ethereumInteractor.ethGasPrice(
            completion = { error, gasPrice ->
                if (gasPrice != null) {
                    eventFlow.value = AsyncEvent.Result(result = gasPrice, error = null)
                } else {
                    eventFlow.value = errorEvent(error?.message ?: "Unknown error")
                }
            },
        )

        return eventFlow
    }
}
