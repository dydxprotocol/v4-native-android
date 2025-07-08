package exchange.dydx.trading.feature.shared.analytics

import android.os.Bundle
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutingAnalytics @Inject constructor(
    private val tracker: Tracking,
) {
    fun logRoute(
        destinationRoute: String,
        arguments: Bundle?
    ) {
        var pathWithArguments = destinationRoute
        if (pathWithArguments.startsWith("market/{marketId}")) {
            // web does not have a /market/<MARKET> path
            pathWithArguments = pathWithArguments.replace("market/{marketId}", "trade/{marketId}")
        }
        arguments?.keySet()?.forEach { key ->
            if (!key.startsWith("android-support-nav:controller:deepLinkIntent")) {
                pathWithArguments =
                    pathWithArguments.replace("{$key}", arguments.getString(key) ?: "")
            }
        }
        if (!pathWithArguments.startsWith("/")) {
            // Android routes don't start with "/"
            pathWithArguments = "/$pathWithArguments"
        }

        tracker.view(
            screenName = pathWithArguments,
            screenClass = "TradingActivity",
        )

        tracker.log(
            event = AnalyticsEvent.NAVIGATE_PAGE.rawValue,
            data = mapOf(
                "path" to pathWithArguments,
            ),
        )
    }
}
