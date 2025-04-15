package exchange.dydx.trading.feature.shared.analytics

import exchange.dydx.dydxstatemanager.clientState.apprating.DydxAppRatingState
import exchange.dydx.trading.feature.shared.apprating.AppRatingState
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import javax.inject.Inject
import javax.inject.Singleton

enum class AppRatingAnalyticsType {
    APP_RATING_COMPLETED,
}

@Singleton
class AppRatingAnalytics @Inject constructor(
    private val tracker: Tracking,
) {
    fun logPrompt(state: DydxAppRatingState) {
        tracker.log(
            event = "PrepromptedForRating",
            data = mapOf(
                "transfers_created_count" to state.transfersCreatedSinceLastPrompt.size,
                "orders_created_count" to state.ordersCreatedSinceLastPrompt.size,
                "unique_day_app_opens_count" to state.uniqueDayAppOpensCount,
                "last_app_open_timestamp" to state.lastAppOpenTimestamp,
                "last_prompted_timestamp" to state.lastPromptedTimestamp,
                "has_ever_connected_wallet" to state.hasEverConnectedWallet,
                "should_stop_preprompting" to state.shouldStopPreprompting,
            ),
        )
    }

    fun logPromptCompleted(response: AppRatingState.ResponseType) {
        val eventName = when (response) {
            AppRatingState.ResponseType.POSITIVE -> "PositiveRatingIntentFollowed"
            AppRatingState.ResponseType.NEGATIVE -> "NegativeRatingIntentFollowed"
            AppRatingState.ResponseType.DISMISSED -> "DeferRatingIntentFollowed"
        }
        tracker.log(
            event = eventName,
            data = emptyMap(),
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
