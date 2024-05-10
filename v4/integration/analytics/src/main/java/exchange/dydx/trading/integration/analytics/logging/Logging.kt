package exchange.dydx.trading.integration.analytics.logging

import exchange.dydx.abacus.protocols.LoggingProtocol
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import exchange.dydx.utilities.utils.Logging

interface CompositeLogging: Logging, LoggingProtocol {
}