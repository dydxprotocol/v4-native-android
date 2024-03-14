package exchange.dydx.trading.integration.analytics

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

    override fun log(event: String, data: String?) {
        trackers.forEach { it.log(event, data) }
    }
}
