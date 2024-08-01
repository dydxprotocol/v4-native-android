package exchange.dydx.trading.integration.statsig

import android.app.Application
import com.statsig.androidsdk.Statsig
import com.statsig.androidsdk.StatsigUser
import exchange.dydx.utilities.utils.WorkerProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class StatsigInitWorker(
    val scope: CoroutineScope,
    val application: Application,
    val sdkKey: String,
) : WorkerProtocol {
    override fun start() {
        if (isStarted) return

        isStarted = true
        scope.launch {
            Statsig.initialize(
                application = application,
                sdkKey = sdkKey,
                user = StatsigUser(Statsig.getStableID()),
            )
        }
    }

    override fun stop() = Unit

    override var isStarted: Boolean = false
}
