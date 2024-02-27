package exchange.dydx.dydxstatemanager.protocolImplementations

import exchange.dydx.abacus.protocols.TrackingProtocol
import exchange.dydx.trading.integration.analytics.Tracking

class AbacusTrackingImp(
    private val nativeTracker: Tracking,
) : TrackingProtocol, Tracking {

    override fun log(event: String, data: String?) {
        nativeTracker.log(event, data)
    }
}
