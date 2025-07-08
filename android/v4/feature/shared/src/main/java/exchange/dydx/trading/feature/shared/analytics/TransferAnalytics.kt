package exchange.dydx.trading.feature.shared.analytics

import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.output.input.TransferInputChainResource
import exchange.dydx.abacus.output.input.TransferInputTokenResource
import exchange.dydx.abacus.utils.filterNotNull
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferAnalytics @Inject constructor(
    private val tracker: Tracking,
) {
    fun logDeposit(transferInput: TransferInput) {
        log(event = AnalyticsEvent.TRANSFER_DEPOSIT, transferInput = transferInput)
    }

    fun logWithdrawal(transferInput: TransferInput) {
        log(event = AnalyticsEvent.TRANSFER_WITHDRAW, transferInput = transferInput)
    }
    private fun log(event: AnalyticsEvent, transferInput: TransferInput) {
        val data: Map<String, String> = mapOf(
            "chainId" to transferInput.chainResource?.chainId?.toString(),
            "tokenAddress" to transferInput.tokenResource?.address,
            "tokenSymbol" to transferInput.tokenResource?.symbol,
            "slippage" to transferInput.summary?.slippage.toString(),
            "gasFee" to transferInput.summary?.gasFee.toString(),
            "bridgeFee" to transferInput.summary?.bridgeFee.toString(),
            "exchangeRate" to transferInput.summary?.exchangeRate.toString(),
            "estimatedRouteDuration" to transferInput.summary?.estimatedRouteDurationSeconds.toString(),
            "toAmount" to transferInput.summary?.toAmount.toString(),
            "toAmountMin" to transferInput.summary?.toAmountMin.toString(),
        ).filterNotNull()

        tracker.log(
            event = event.rawValue,
            data = data,
        )
    }
}

private val TransferInput.chainResource: TransferInputChainResource?
    get() = chain?.let { chain ->
        resources?.chainResources?.get(chain)
    }

private val TransferInput.tokenResource: TransferInputTokenResource?
    get() = token?.let { token ->
        resources?.tokenResources?.get(token)
    }
