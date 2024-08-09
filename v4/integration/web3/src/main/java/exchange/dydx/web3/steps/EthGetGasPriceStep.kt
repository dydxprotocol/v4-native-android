package exchange.dydx.web3.steps

import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.web3.EthereumInteractor
import java.math.BigInteger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EthGetGasPriceStep(
    private val ethereumInteractor: EthereumInteractor,
) : AsyncStep<BigInteger> {

    override suspend fun run(): Result<BigInteger> = suspendCoroutine { continuation ->
        ethereumInteractor.ethGasPrice(
            completion = { error, gasPrice ->
                if (gasPrice != null) {
                    continuation.resume(Result.success(gasPrice))
                } else {
                    continuation.resume((errorEvent(error?.message ?: "Unknown error")))
                }
            },
        )
    }
}
