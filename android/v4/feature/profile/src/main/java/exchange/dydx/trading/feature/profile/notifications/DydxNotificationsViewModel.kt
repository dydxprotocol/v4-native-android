package exchange.dydx.trading.feature.profile.notifications

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.settings.PlatformUISettings
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.NotificationEnabled
import exchange.dydx.trading.feature.shared.views.SettingsView
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

private val settingsFile = "settings_notifications.json"

@HiltViewModel
class DydxNotificationsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    @ApplicationContext private val appContext: Context,
    private val router: DydxRouter,
    private val preferencesStore: SharedPreferencesStore,
) : ViewModel(), DydxViewModel {

    private val mutableState = MutableStateFlow(createViewState())

    val state: Flow<SettingsView.ViewState?> = mutableState

    private fun createViewState(): SettingsView.ViewState {
        val settings = PlatformUISettings.loadFromAssets(appContext, settingsFile)

        var viewState = SettingsView.ViewState.createFrom(
            settings = settings,
            localizer = localizer,
            header = localizer.localize("APP.V4.NOTIFICATIONS"),
            backAction = {
                router.navigateBack()
            },
            itemFieldAction = { _, value ->
                NotificationEnabled.update(preferencesStore, value)
                mutableState.value = createViewState()
            },
        )

        viewState.sections.first().items.first().value = NotificationEnabled.currentValue(preferencesStore)

        return viewState
    }

    companion object {
        fun currentValueText(
            localizer: LocalizerProtocol,
            preferencesStore: SharedPreferencesStore,
        ): String? {
            val notificationsOn = NotificationEnabled.enabled(preferencesStore)
            return if (notificationsOn) localizer.localize("APP.HEADER.ON") else localizer.localize("APP.HEADER.OFF")
        }
    }
}
