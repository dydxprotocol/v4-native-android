package exchange.dydx.trading.feature.workers.globalworkers

import android.content.Context
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.ProfileRoutes
import exchange.dydx.utilities.utils.Logging
import exchange.dydx.utilities.utils.VersionUtils
import exchange.dydx.utilities.utils.WorkerProtocol
import exchange.dydx.utilities.utils.delayFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration.Companion.seconds

private const val TAG = "DydxUpdateWorker"

class DydxUpdateWorker(
    private val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val context: Context,
    private val logger: Logging,
) : WorkerProtocol {
    override var isStarted = false

    override fun start() {
        if (!isStarted) {
            isStarted = true

            router.initialized
                .filter { it }
                .flatMapLatest {
                    // Wait 2 seconds for the root view to load first.  Otherwise, "/portfolio/" would take
                    // place after "/update"
                    delayFlow(duration = 2.seconds)
                }
                .flatMapLatest { abacusStateManager.currentEnvironmentId }
                .mapNotNull { it }
                .distinctUntilChanged()
                .onEach {
                    abacusStateManager.environment?.let {
                        update(it)
                    } ?: run {
                        logger.e(TAG, "Environment is null")
                    }
                }
                .launchIn(scope)
        }
    }

    override fun stop() {
        if (isStarted) {
            isStarted = false
        }
    }

    private fun update(environment: V4Environment) {
        val desired = environment.apps?.android?.build ?: return
        val mine = VersionUtils.versionCode(context) ?: return
        if (desired > mine) {
            router.navigateToRoot(excludeRoot = true)
            router.navigateTo(ProfileRoutes.update)
        }
    }
}
