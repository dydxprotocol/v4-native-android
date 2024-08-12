package exchange.dydx.trading.feature.transfer.deposit

import android.content.Context
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.utilities.utils.runWithLogs
import exchange.dydx.web3.EthereumInteractor
import exchange.dydx.web3.steps.EthGetERC20AllowanceStep
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
) : AsyncStep<Boolean> {

    override suspend fun run(): Result<Boolean> {
        if (tokenAddress == "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE") {
            return Result.success(true)
        }

        val event = EthGetERC20AllowanceStep(
            ethereumInteractor = EthereumInteractor(chainRpc),
            tokenAddress = tokenAddress,
            ethereumAddress = ethereumAddress,
            spenderAddress = spenderAddress,
        ).runWithLogs()

        if (event.isFailure) return errorEvent(event.exceptionOrNull()?.message ?: "unknown error")

        val allowance = event.getOrThrow()
        if (allowance >= desiredAmount) {
            return Result.success(true)
        } else {
            return ERC20ApprovalStep(
                chainRpc = chainRpc,
                tokenAddress = tokenAddress,
                ethereumAddress = ethereumAddress,
                spenderAddress = spenderAddress,
                desiredAmount = desiredAmount,
                walletId = walletId,
                chainId = chainId,
                provider = provider,
                context = context,
            ).runWithLogs()
        }
    }
}
