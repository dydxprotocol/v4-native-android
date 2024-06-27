package exchange.dydx.dydxstatemanager

import exchange.dydx.abacus.output.PerpetualMarket

val PerpetualMarket?.maxLeverage: Double
    get() {
        if (this == null) return 5.0
        val imf = configs?.run { effectiveInitialMarginFraction ?: initialMarginFraction }
        return imf?.let { 1.0 / it } ?: 5.0
    }
