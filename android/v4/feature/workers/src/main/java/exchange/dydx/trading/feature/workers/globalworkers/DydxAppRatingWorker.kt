package exchange.dydx.trading.feature.workers.globalworkers

import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.feature.shared.apprating.AppRatingState
import exchange.dydx.utilities.utils.WorkerProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@ActivityRetainedScoped
class DydxAppRatingWorker @Inject constructor(
    @CoroutineScopes.App private val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val appRatingState: AppRatingState,
) : WorkerProtocol {
    override var isStarted = false

    override fun start() {
        if (!isStarted) {
            isStarted = true
        }

        abacusStateManager.state.currentWallet
            .mapNotNull { it }
            .onEach { wallet ->
                appRatingState.connectedWallet()
            }
            .launchIn(scope)

        abacusStateManager.state.transfers.mapNotNull { it }
            .onEach { transfers ->
                for (transfer in transfers) {
                    appRatingState.transferCreated(transfer.id, transfer.updatedAtMilliseconds)
                }
            }
            .launchIn(scope)

        abacusStateManager.state.selectedSubaccountFills
            .mapNotNull { it }
            .onEach { fills ->
                for (fill in fills) {
                    appRatingState.orderCreated(fill.id, fill.createdAtMilliseconds)
                }
            }
            .launchIn(scope)
    }

    override fun stop() {
        if (isStarted) {
            isStarted = false
        }
    }
}
