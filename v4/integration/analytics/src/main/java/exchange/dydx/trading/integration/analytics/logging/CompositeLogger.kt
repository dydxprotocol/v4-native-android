package exchange.dydx.trading.integration.analytics.logging

import exchange.dydx.abacus.protocols.LoggingProtocol
import javax.inject.Inject

interface CompositeLogging : exchange.dydx.utilities.utils.Logging, LoggingProtocol
class CompositeLogger @Inject constructor(
    private val consoleLogger: ConsoleLogger,
    private val crashlyticsLogger: CrashlyticsLogger
) : CompositeLogging {
    private val loggers: List<LoggingProtocol> = listOf(consoleLogger, crashlyticsLogger)

    override fun d(tag: String, message: String) {
        loggers.forEach { it.d(tag, message) }
    }

    override fun e(tag: String, message: String) {
        loggers.forEach { it.e(tag, message) }
    }
}
