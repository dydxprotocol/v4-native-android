package exchange.dydx.trading.integration.analytics.logging

import exchange.dydx.utilities.utils.Logging
import javax.inject.Inject

class ConsoleLogger @Inject constructor() : Logging {
    override fun d(tag: String, message: String) {
        println("$tag: $message")
    }

    override fun e(tag: String, message: String) {
        System.err.println("$tag: $message")
    }
}
