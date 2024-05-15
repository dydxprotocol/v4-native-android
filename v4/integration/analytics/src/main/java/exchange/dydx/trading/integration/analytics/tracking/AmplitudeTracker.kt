package exchange.dydx.trading.integration.analytics.tracking

import com.amplitude.android.Amplitude
import com.amplitude.android.events.Identify
import exchange.dydx.utilities.utils.jsonStringToMap

class AmplitudeTracker(
    private val amplitude: Amplitude
) : Tracking {
    override fun setUserId(userId: String?) {
        amplitude.setUserId(userId)
    }

    override fun setUserProperties(properties: Map<String, String?>) {
        val identify = Identify()
        properties.forEach { (key, value) ->
            if (value == null) {
                identify.unset(key)
            } else {
                identify.set(key, value)
            }
        }
        amplitude.identify(identify)
    }

    override fun log(event: String, data: String?) {
        val jsonMap = data?.jsonStringToMap()
        amplitude.track(event, jsonMap)
    }
}
