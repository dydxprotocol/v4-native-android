package exchange.dydx.trading.feature.shared.analytics

import exchange.dydx.abacus.functional.ClientTrackableEventType
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
        tracker.logSharedEvent(
            ClientTrackableEventType.VaultFormPreviewStep(
                type = type.name,
                amount = amount,
            ),
        )
    }

    fun logOperationAttempt(
        type: VaultAnalyticsInputType,
        amount: Double?,
        slippage: Double?,
    ) {
        tracker.logSharedEvent(
            ClientTrackableEventType.AttemptVaultOperation(
                type = type.name,
                amount = amount,
                slippage = slippage,
            ),
        )
    }

    fun logOperationSuccess(
        type: VaultAnalyticsInputType,
        amount: Double?,
        amountDiff: Double?,
    ) {
        tracker.logSharedEvent(
            ClientTrackableEventType.SuccessfulVaultOperation(
                type = type.name,
                amount = amount ?: 0.0,
                amountDiff = amountDiff ?: 0.0,
            ),
        )
    }

    fun logOperationFailure(
        type: VaultAnalyticsInputType,
    ) {
        tracker.logSharedEvent(
            ClientTrackableEventType.VaultOperationProtocolError(
                type = type.name,
            ),
        )
    }
}
