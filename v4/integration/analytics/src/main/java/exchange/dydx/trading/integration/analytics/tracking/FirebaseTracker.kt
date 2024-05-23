package exchange.dydx.trading.integration.analytics.tracking

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import exchange.dydx.utilities.utils.jsonStringToMap

class FirebaseTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) : Tracking {

    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }

    override fun setUserProperties(properties: Map<String, String?>) {
        properties.forEach { (key, value) ->
            firebaseAnalytics.setUserProperty(key, value)
        }
    }

    override fun log(event: String, data: String?) {
        firebaseAnalytics.logEvent(event) {
            val jsonMap = data?.jsonStringToMap()
            jsonMap?.forEach { (key, value) ->
                param(key, value.toString())
            }
        }
    }

    override fun view(screenName: String, screenClass: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
    }
}
