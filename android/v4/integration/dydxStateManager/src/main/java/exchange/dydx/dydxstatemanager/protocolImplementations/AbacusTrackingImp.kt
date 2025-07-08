package exchange.dydx.dydxstatemanager.protocolImplementations

import exchange.dydx.abacus.protocols.TrackingProtocol
import exchange.dydx.trading.integration.analytics.tracking.CompositeTracking
import javax.inject.Inject

class AbacusTrackingImp @Inject constructor(
    private val compositeTracking: CompositeTracking,
) : TrackingProtocol {

    override fun log(event: String, data: String?) {
        compositeTracking.log(event, data)
    }
}
