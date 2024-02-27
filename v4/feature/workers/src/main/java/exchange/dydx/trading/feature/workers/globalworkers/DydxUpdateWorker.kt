package exchange.dydx.trading.feature.workers.globalworkers

import android.content.Context
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.ProfileRoutes
import exchange.dydx.utilities.utils.VersionUtils
import exchange.dydx.utilities.utils.WorkerProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

class DydxUpdateWorker(
    override val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val context: Context,
) : WorkerProtocol {
    override var isStarted = false

    override fun start() {
        if (!isStarted) {
            isStarted = true

            abacusStateManager.currentEnvironmentId
                .mapNotNull { it }
                .distinctUntilChanged()
                .onEach {
                    abacusStateManager.environment?.let {
                        update(it)
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

    private fun update(environment: V4Environment?) {
        val desired = environment?.apps?.android?.build ?: return
        val mine = VersionUtils.versionCode(context) ?: return
        if (desired > mine) {
            router.navigateTo(ProfileRoutes.update)
        }
    }
}
