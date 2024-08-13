package exchange.dydx.trading.feature.transfer.deposit

import android.content.Context
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.cartera.walletprovider.EthereumTransactionRequest
import exchange.dydx.dydxCartera.steps.WalletSendTransactionStep
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.utilities.utils.runWithLogs
import exchange.dydx.web3.ABIEncoder
import java.math.BigInteger

class ERC20ApprovalStep(
    private val tokenAddress: String,
    private val ethereumAddress: String,
    private val spenderAddress: String,
    private val desiredAmount: BigInteger?,
    private val walletId: String?,
    private val chainId: String,
    private val provider: CarteraProvider,
    private val context: Context,
) : AsyncStep<Boolean> {

    override suspend fun run(): Result<Boolean> {
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
            nonce = null,
            gasPriceInWei = null,
            maxFeePerGas = null,
            maxPriorityFeePerGas = null,
            gasLimit = null,
            chainId = chainId,
        )

        return WalletSendTransactionStep(
            transaction = transaction,
            chainId = chainId,
            walletAddress = ethereumAddress,
            walletId = walletId,
            context = context,
            provider = provider,
        ).runWithLogs().map { true }
    }
}
