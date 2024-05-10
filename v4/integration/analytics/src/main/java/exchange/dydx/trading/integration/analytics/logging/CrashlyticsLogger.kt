package exchange.dydx.trading.integration.analytics.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import exchange.dydx.abacus.protocols.LoggingProtocol
import javax.inject.Inject

class CrashlyticsLogger @Inject constructor() : LoggingProtocol {
    override fun d(tag: String, message: String) {
        // No-op
    }

    override fun e(tag: String, message: String) {
        FirebaseCrashlytics.getInstance().recordException(Exception("$tag: $message"))
    }
}