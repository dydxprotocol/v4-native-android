package exchange.dydx.trading.feature.workers.globalworkers

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.protocols.AbacusLocalizerProtocol
import exchange.dydx.dydxCartera.CarteraConfig
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.feature.shared.analytics.UserProperty
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import exchange.dydx.utilities.utils.WorkerProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.komputing.khash.sha256.extensions.sha256
import javax.inject.Inject

@ActivityRetainedScoped
class DydxUserTrackingWorker @Inject constructor(
    @CoroutineScopes.App private val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val localizer: AbacusLocalizerProtocol,
    private val tracker: Tracking,
    private val application: Application,
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
                    val address = it?.ethereumAddress ?: it?.cosmoAddress
                    val wallet = CarteraConfig.shared?.wallets?.firstOrNull { wallet -> wallet.id == it?.walletId }
                    if (it?.walletId == "turnkey") {
                        val userId = it.userEmail?.trim()?.lowercase()?.sha256()?.joinToString("") { "%02x".format(it) }
                        tracker.setUserId(userId ?: address)
                        tracker.setUserProperties(
                            mapOf(
                                UserProperty.walletType.rawValue to it.walletId?.uppercase(),
                                UserProperty.dydxAddress.rawValue to it.cosmoAddress,
                            ),
                        )
                    } else {
                        tracker.setUserId(address)
                        tracker.setUserProperties(
                            mapOf(
                                UserProperty.walletType.rawValue to wallet?.userFields?.get("analyticEvent"),
                                UserProperty.dydxAddress.rawValue to it?.cosmoAddress,
                            ),
                        )
                    }
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

            val pushEnabled = ContextCompat.checkSelfPermission(application, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            tracker.setUserProperties(
                mapOf(
                    "pushNotificationsEnabled" to pushEnabled.toString(),
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
