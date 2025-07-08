package exchange.dydx.trading.core

import androidx.fragment.app.FragmentActivity
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.DefaultTrackingOptions
import com.amplitude.core.ServerZone
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import exchange.dydx.trading.common.R
import exchange.dydx.trading.integration.analytics.tracking.AmplitudeTracker
import exchange.dydx.trading.integration.analytics.tracking.CompositeTracking
import exchange.dydx.trading.integration.analytics.tracking.FirebaseTracker
import exchange.dydx.utilities.utils.Logging

object AnalyticsSetup {

    private const val TAG = "AnalyticsSetup"

    fun run(
        compositeTracking: CompositeTracking,
        activity: FragmentActivity,
        logger: Logging,
    ) {
        try {
            val firebaseAnalytics = Firebase.analytics
            compositeTracking.addTracker(FirebaseTracker(firebaseAnalytics))

            val serverZone = activity.applicationContext.getString(R.string.amplitude_server_zone)
            val amplitude = Amplitude(
                Configuration(
                    apiKey = activity.applicationContext.getString(R.string.amplitude_api_key),
                    context = activity.applicationContext,
                    defaultTracking = DefaultTrackingOptions.ALL,
                    serverZone = if (serverZone == "EU") ServerZone.EU else ServerZone.US,
                ),
            )
            compositeTracking.addTracker(AmplitudeTracker(amplitude))
        } catch (e: Exception) {
            logger.e(TAG, "Failed to set up Analytics")
        }
    }
}
