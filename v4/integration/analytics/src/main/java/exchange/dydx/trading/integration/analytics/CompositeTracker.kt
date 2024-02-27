package exchange.dydx.trading.integration.analytics

class CompositeTracker : CompositeTracking {
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
