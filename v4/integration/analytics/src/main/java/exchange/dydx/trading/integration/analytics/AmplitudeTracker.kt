package exchange.dydx.trading.integration.analytics

import com.amplitude.android.Amplitude
import exchange.dydx.utilities.utils.jsonStringToMap

class AmplitudeTracker(
    private val amplitude: Amplitude
) : Tracking {

    override fun log(event: String, data: String?) {
        val jsonMap = data?.jsonStringToMap()
        amplitude.track(event, jsonMap)
    }
}
