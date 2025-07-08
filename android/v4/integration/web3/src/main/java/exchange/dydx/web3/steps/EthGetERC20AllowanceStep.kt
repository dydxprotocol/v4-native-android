package exchange.dydx.web3.steps

import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.web3.EthereumInteractor
import java.math.BigInteger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EthGetERC20AllowanceStep(
    private val ethereumInteractor: EthereumInteractor,
    private val tokenAddress: String,
    private val ethereumAddress: String,
    private val spenderAddress: String,
) : AsyncStep<BigInteger> {

    override suspend fun run(): Result<BigInteger> = suspendCoroutine { continuation ->
        ethereumInteractor.erc20GetAllowance(
            tokenAddress = tokenAddress,
            ownerAddress = ethereumAddress,
            spenderAddress = spenderAddress,
            completion = { error, allowance ->
                if (allowance != null) {
                    continuation.resume(Result.success(allowance))
                } else {
                    continuation.resume(errorEvent(error?.message ?: "Unknown error"))
                }
            },
        )
    }
}
