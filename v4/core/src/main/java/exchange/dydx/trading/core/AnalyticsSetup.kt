package exchange.dydx.trading.core

import androidx.fragment.app.FragmentActivity
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.DefaultTrackingOptions
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import exchange.dydx.trading.common.R
import exchange.dydx.trading.integration.analytics.tracking.AmplitudeTracker
import exchange.dydx.trading.integration.analytics.tracking.CompositeTracking
import exchange.dydx.trading.integration.analytics.tracking.FirebaseTracker
import timber.log.Timber

object AnalyticsSetup {

    private const val TAG = "AnalyticsSetup"

    fun run(
        compositeTracking: CompositeTracking,
        activity: FragmentActivity,
    ) {
        try {
            val firebaseAnalytics = Firebase.analytics
            compositeTracking.addTracker(FirebaseTracker(firebaseAnalytics))

            val amplitude = Amplitude(
                Configuration(
                    apiKey = activity.applicationContext.getString(R.string.amplitude_api_key),
                    context = activity.applicationContext,
                    defaultTracking = DefaultTrackingOptions.ALL,
                ),
            )
            compositeTracking.addTracker(AmplitudeTracker(amplitude))
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to set up Analytics")
        }
    }
}
