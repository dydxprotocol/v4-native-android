package exchange.dydx.trading.integration.analytics.tracking

import exchange.dydx.abacus.protocols.TrackingProtocol
import exchange.dydx.abacus.utils.toJson
import exchange.dydx.trading.common.AsyncResult.Waiting.data

interface Tracking : TrackingProtocol {

    fun setUserId(userId: String?)

    fun setUserProperties(properties: Map<String, String?>)

    override fun log(event: String, data: String?)

    fun log(event: String, data: Map<String, Any?>) {
        log(event, data.toJson())
    }

    fun view(screenName: String, screenClass: String)
}

interface CompositeTracking : Tracking {
    fun addTracker(tracker: Tracking)
    fun removeTracker(tracker: Tracking)
}
