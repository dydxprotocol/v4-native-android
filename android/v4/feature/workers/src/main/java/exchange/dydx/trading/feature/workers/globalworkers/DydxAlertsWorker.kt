package exchange.dydx.trading.feature.workers.globalworkers

import android.system.Os.link
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.PlatformInfoViewModel
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.featureflags.DydxBoolFeatureFlag
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.NotificationEnabled
import exchange.dydx.utilities.utils.SharedPreferencesStore
import exchange.dydx.utilities.utils.WorkerProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@ActivityRetainedScoped
class DydxAlertsWorker @Inject constructor(
    @CoroutineScopes.App private val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
    private val toaster: PlatformInfo,
    private val preferencesStore: SharedPreferencesStore,
    private val featureFlags: DydxFeatureFlags,
) : WorkerProtocol {
    private val handledAlertHashes = mutableSetOf<String>()

    override var isStarted = false

    override fun start() {
        if (!isStarted) {
            isStarted = true

            abacusStateManager.state.alerts
                .mapNotNull { it }
                .onEach { alerts ->
                    updateAlerts(alerts)
                }
                .launchIn(scope)
        }
    }

    override fun stop() {
        if (isStarted) {
            isStarted = false
        }
    }

    private fun updateAlerts(alerts: List<exchange.dydx.abacus.output.Notification>) {
        alerts
            // don't display an alert which has already been handled
            .filter { !handledAlertHashes.contains(it.id) }
            // display alerts in chronological order they were received
            .sortedBy { it.updateTimeInMilliseconds }
            .forEach { alert ->
                val alertText = alert.text ?: return@forEach

                if (!NotificationEnabled.enabled(preferencesStore)) {
                    return@forEach
                }

                if (featureFlags.isFeatureEnabled(DydxBoolFeatureFlag.ff_rewards_sep_2025) &&
                    alert.id.startsWith("blockReward:")
                ) {
                    return@forEach
                }

                val link = alert.link
                toaster.show(
                    title = alert.title,
                    message = alertText,
                    buttonTitle = if (link != null) localizer.localize("APP.GENERAL.VIEW") else null,
                    type = alert.type.infoType,
                    buttonAction = if (link != null) {
                        {
                            router.navigateTo(link)
                        }
                    } else {
                        null
                    },
                )

                // add to alert ids set to avoid double handling
                handledAlertHashes.add(alert.id)
            }
    }
}

private val exchange.dydx.abacus.output.NotificationType.infoType: PlatformInfoViewModel.Type
    get() = when (this) {
        exchange.dydx.abacus.output.NotificationType.INFO -> PlatformInfoViewModel.Type.Info
        exchange.dydx.abacus.output.NotificationType.WARNING -> PlatformInfoViewModel.Type.Warning
        exchange.dydx.abacus.output.NotificationType.ERROR -> PlatformInfoViewModel.Type.Error
    }
