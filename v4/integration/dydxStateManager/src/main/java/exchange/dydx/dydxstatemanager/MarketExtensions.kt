package exchange.dydx.dydxstatemanager

import exchange.dydx.abacus.output.PerpetualMarket

val PerpetualMarket?.maxLeverage: Double
    get() {
        val imf = this?.configs?.run { effectiveInitialMarginFraction ?: initialMarginFraction }
            .takeIf { it != 0.0 }
        return imf?.let { 1.0 / it } ?: 1.0
    }
