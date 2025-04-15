package exchange.dydx.trading.feature.shared.analytics

import exchange.dydx.trading.feature.shared.apprating.AppRatingState
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import javax.inject.Inject
import javax.inject.Singleton

enum class AppRatingAnalyticsType {
    APP_RATING_PROMPTED,
    APP_RATING_COMPLETED,
}

@Singleton
class AppRatingAnalytics @Inject constructor(
    private val tracker: Tracking,
) {
    fun logPrompted(response: AppRatingState.ResponseType) {
        tracker.log(
            event = AppRatingAnalyticsType.APP_RATING_PROMPTED.name,
            data = mapOf(
                "response" to response.name,
            ),
        )
    }

    fun logCompleted(isSuccess: Boolean, errorCode: Int?) {
        tracker.log(
            event = AppRatingAnalyticsType.APP_RATING_COMPLETED.name,
            data = mapOf(
                "isSuccess" to isSuccess.toString(),
                "errorCode" to (errorCode?.toString() ?: "null"),
            ),
        )
    }
}
