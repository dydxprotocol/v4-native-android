package exchange.dydx.trading.feature.shared.analytics

import exchange.dydx.abacus.functional.ClientTrackableEvent
import exchange.dydx.trading.integration.analytics.tracking.Tracking

fun Tracking.logSharedEvent(event: ClientTrackableEvent) {
    log(
        event = event.name,
        data = event.customParameters,
    )
}
