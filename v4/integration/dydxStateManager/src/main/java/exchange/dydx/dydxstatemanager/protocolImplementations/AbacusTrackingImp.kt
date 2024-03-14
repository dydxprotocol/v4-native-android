package exchange.dydx.dydxstatemanager.protocolImplementations

import exchange.dydx.abacus.protocols.TrackingProtocol
import exchange.dydx.trading.integration.analytics.CompositeTracking
import exchange.dydx.trading.integration.analytics.Tracking
import javax.inject.Inject

class AbacusTrackingImp @Inject constructor(
    private val compositeTracking: CompositeTracking,
) : TrackingProtocol, Tracking {

    override fun log(event: String, data: String?) {
        compositeTracking.log(event, data)
    }
}
