package exchange.dydx.trading.integration.statsig

import android.app.Application
import com.statsig.androidsdk.Statsig
import com.statsig.androidsdk.StatsigUser
import exchange.dydx.trading.common.R
import exchange.dydx.utilities.utils.WorkerProtocol
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Needs to be initialized before Abacus
 */
@Singleton
class StatsigInitWorker @Inject constructor(
    val application: Application,
) : WorkerProtocol {

    override fun start() {
        if (isStarted) return

        isStarted = true
        val user = StatsigUser()
        user.customIDs = mapOf("isNativeApp" to "true")
        Statsig.initializeAsync(
            application = application,
            sdkKey = application.getString(R.string.statsig_api_key),
            user = user,
        )
    }

    override fun stop() = Unit

    override var isStarted: Boolean = false
}
