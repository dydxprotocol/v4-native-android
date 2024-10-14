package exchange.dydx.trading.feature.shared.analytics

import exchange.dydx.abacus.utils.filterNotNull
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import javax.inject.Inject
import javax.inject.Singleton

enum class VaultAnalyticsInputType {
    DEPOSIT,
    WITHDRAW
}

@Singleton
class VaultAnalytics @Inject constructor(
    private val tracker: Tracking,
) {
    fun logPreview(
        type: VaultAnalyticsInputType,
        amount: Double
    ) {
        tracker.log(
            event = AnalyticsEvent.VaultFormPreviewStep.rawValue,
            data = mapOf(
                "operation" to type.name,
                "amount" to amount.toString(),
            ),
        )
    }

    fun logOperationAttempt(
        type: VaultAnalyticsInputType,
        amount: Double?,
        slippage: Double?,
    ) {
        tracker.log(
            event = AnalyticsEvent.AttemptVaultOperation.rawValue,
            data = mapOf(
                "operation" to type.name,
                "amount" to amount.toString(),
                "slippage" to slippage.toString(),
            ).filterNotNull(),
        )
    }

    fun logOperationSuccess(
        type: VaultAnalyticsInputType,
        amount: Double?,
        amountDiff: Double?,
    ) {
        tracker.log(
            event = AnalyticsEvent.SuccessfulVaultOperation.rawValue,
            data = mapOf(
                "operation" to type.name,
                "amount" to amount.toString(),
                "amountDiff" to amountDiff.toString(),
            ).filterNotNull(),
        )
    }

    fun logOperationFailure(
        type: VaultAnalyticsInputType,
    ) {
        tracker.log(
            event = AnalyticsEvent.VaultOperationProtocolError.rawValue,
            data = mapOf(
                "operation" to type.name,
            ),
        )
    }
}
