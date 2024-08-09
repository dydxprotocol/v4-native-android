package exchange.dydx.web3.steps

import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.web3.EthereumInteractor
import java.math.BigInteger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EthGetNonceStep(
    private val address: String,
    private val ethereumInteractor: EthereumInteractor,
) : AsyncStep<BigInteger> {

    override suspend fun run(): Result<BigInteger> = suspendCoroutine { continuation ->
        ethereumInteractor.ethGetTransactionCount(
            address = address,
            completion = { error, count ->
                if (count != null) {
                    continuation.resume(Result.success(count))
                } else {
                    continuation.resume((errorEvent(error?.message ?: "Unknown error")))
                }
            },
        )
    }
}
