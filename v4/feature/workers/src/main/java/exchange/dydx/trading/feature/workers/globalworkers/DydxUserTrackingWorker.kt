package exchange.dydx.trading.feature.workers.globalworkers

import exchange.dydx.abacus.protocols.AbacusLocalizerProtocol
import exchange.dydx.cartera.CarteraConfig
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.feature.shared.analytics.UserProperty
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import exchange.dydx.utilities.utils.WorkerProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class DydxUserTrackingWorker(
    private val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val localizer: AbacusLocalizerProtocol,
    private val tracker: Tracking,
) : WorkerProtocol {
    override var isStarted = false

    override fun start() {
        if (!isStarted) {
            isStarted = true

            abacusStateManager.currentEnvironmentId
                .filterNotNull()
                .onEach {
                    tracker.setUserProperties(
                        mapOf(
                            UserProperty.network.rawValue to it,
                        ),
                    )
                }
                .launchIn(scope)

            abacusStateManager.state.currentWallet
                .onEach {
                    tracker.setUserId(it?.ethereumAddress ?: it?.cosmoAddress)
                    val wallet = CarteraConfig.shared?.wallets?.firstOrNull { wallet -> wallet.id == it?.walletId }
                    tracker.setUserProperties(
                        mapOf(
                            UserProperty.walletType.rawValue to wallet?.userFields?.get("analyticEvent"),
                            UserProperty.dydxAddress.rawValue to it?.cosmoAddress,
                        ),
                    )
                }
                .launchIn(scope)

            abacusStateManager.state.selectedSubaccount
                .map { it?.subaccountNumber }
                .distinctUntilChanged()
                .onEach {
                    tracker.setUserProperties(
                        mapOf(
                            UserProperty.subaccountNumber.rawValue to it?.let { subaccountNumber -> subaccountNumber?.toString() },
                        ),
                    )
                }
                .launchIn(scope)

            tracker.setUserProperties(
                mapOf(
                    UserProperty.selectedLocale.rawValue to localizer.language,
                ),
            )
        }
    }

    override fun stop() {
        if (isStarted) {
            isStarted = false
        }
    }
}
