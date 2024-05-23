package exchange.dydx.trading.integration.analytics.tracking

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompositeTracker @Inject constructor() : CompositeTracking {
    private val trackers = mutableListOf<Tracking>()

    override fun addTracker(tracker: Tracking) {
        trackers.add(tracker)
    }

    override fun removeTracker(tracker: Tracking) {
        trackers.remove(tracker)
    }

    override fun setUserId(userId: String?) {
        trackers.forEach { it.setUserId(userId) }
    }

    override fun setUserProperties(properties: Map<String, String?>) {
        trackers.forEach { it.setUserProperties(properties) }
    }

    override fun log(event: String, data: String?) {
        trackers.forEach { it.log(event, data) }
    }

    override fun view(screenName: String, screenClass: String) {
        trackers.forEach { it.view(screenName, screenClass) }
    }
}
