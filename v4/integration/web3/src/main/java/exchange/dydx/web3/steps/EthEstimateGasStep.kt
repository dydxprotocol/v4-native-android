package exchange.dydx.web3.steps

import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.web3.EthereumInteractor
import java.math.BigInteger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EthEstimateGasStep(
    private val ethereumInteractor: EthereumInteractor,
) : AsyncStep<BigInteger?> {

    override suspend fun run(): Result<BigInteger?> = suspendCoroutine { continuation ->
        ethereumInteractor.ethEstimateGas(
            completion = { error, gasPrice ->
                if (error != null) {
                    continuation.resume((errorEvent(error.message ?: "Unknown error")))
                } else {
                    continuation.resume(Result.success(gasPrice))
                }
            },
        )
    }
}
