package exchange.dydx.trading.integration.analytics.logging

import exchange.dydx.abacus.protocols.LoggingProtocol
import exchange.dydx.utilities.utils.Logging
import javax.inject.Inject

interface CompositeLogging : exchange.dydx.utilities.utils.Logging, LoggingProtocol
class CompositeLogger @Inject constructor(
    private val consoleLogger: ConsoleLogger,
    private val crashlyticsLogger: CrashlyticsLogger
) : CompositeLogging {
    private val loggers: List<Logging> = listOf(consoleLogger, crashlyticsLogger)

    override fun d(tag: String, message: String) {
        loggers.forEach { it.d(tag, message) }
    }

    override fun e(tag: String, message: String) {
        loggers.forEach { it.e(tag, message) }
    }

    override fun e(tag: String, message: String, context: Map<String, Any>?, error: Error?) {
        loggers.forEach { it.e(tag, message) }
    }

    override fun ddInfo(tag: String, message: String, context: Map<String, Any>?) {
        // no-op for now
    }
}
