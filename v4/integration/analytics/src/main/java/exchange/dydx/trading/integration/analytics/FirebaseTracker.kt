package exchange.dydx.trading.integration.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import exchange.dydx.utilities.utils.jsonStringToMap

class FirebaseTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) : Tracking {

    override fun log(event: String, data: String?) {
        firebaseAnalytics.logEvent(event) {
            val jsonMap = data?.jsonStringToMap()
            jsonMap?.forEach { (key, value) ->
                param(key, value.toString())
            }
        }
    }
}
