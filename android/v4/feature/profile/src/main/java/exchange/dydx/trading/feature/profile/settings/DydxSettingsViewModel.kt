package exchange.dydx.trading.feature.profile.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import exchange.dydx.abacus.protocols.AbacusLocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.settings.PlatformUISettings
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.profile.color.DydxDirectionColorPreferenceViewModel
import exchange.dydx.trading.feature.profile.gastoken.DydxGasTokenViewModel
import exchange.dydx.trading.feature.profile.language.DydxLanguageViewModel
import exchange.dydx.trading.feature.profile.notifications.DydxNotificationsViewModel
import exchange.dydx.trading.feature.profile.theme.DydxThemeViewModel
import exchange.dydx.trading.feature.profile.tradingnetwork.DydxTradingNetworkViewModel
import exchange.dydx.trading.feature.shared.PreferenceKeys
import exchange.dydx.trading.feature.shared.views.SettingsView
import exchange.dydx.utilities.utils.DebugEnabled
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxSettingsViewModel @Inject constructor(
    private val appConfig: AppConfig,
    private val localizer: AbacusLocalizerProtocol,
    @ApplicationContext private val appContext: Context,
    private val preferencesStore: SharedPreferencesStore,
    private val router: DydxRouter,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<SettingsView.ViewState?> = preferencesStore.stateUpdatedCount.map { createViewState() }

    private fun createViewState(): SettingsView.ViewState {
        val settingsFile = if (DebugEnabled.enabled(preferencesStore)) {
            "settings_debug.json"
        } else {
            "settings.json"
        }
        val settings = PlatformUISettings.loadFromAssets(appContext, settingsFile)
        return SettingsView.ViewState.createFrom(
            settings = settings,
            localizer = localizer,
            header = localizer.localize("APP.EMAIL_NOTIFICATIONS.SETTINGS"),
            footer = appConfig.appVersionName + " (" + appConfig.appVersionCode + ")",
            backAction = {
                router.navigateBack()
            },
            itemAction = { link ->
                router.navigateTo(
                    route = link,
                    presentation = DydxRouter.Presentation.Push,
                )
            },
            valueOfField = { field ->
                when (field.field) {
                    PreferenceKeys.Language -> {
                        return@createFrom DydxLanguageViewModel.currentValueText(
                            localizer = localizer,
                            preferencesStore = preferencesStore,
                        )
                    }
                    PreferenceKeys.Theme -> {
                        return@createFrom DydxThemeViewModel.currentValueText(
                            localizer = localizer,
                            appContext = appContext,
                            preferencesStore = preferencesStore,
                        )
                    }
                    PreferenceKeys.DirectionColor -> {
                        return@createFrom DydxDirectionColorPreferenceViewModel.currentValueText(
                            localizer = localizer,
                            preferencesStore = preferencesStore,
                        )
                    }
                    PreferenceKeys.Env -> {
                        return@createFrom DydxTradingNetworkViewModel.currentValueText(
                            localizer = localizer,
                            abacusStateManager = abacusStateManager,
                        )
                    }
                    PreferenceKeys.Notifications -> {
                        return@createFrom DydxNotificationsViewModel.currentValueText(
                            localizer = localizer,
                            preferencesStore = preferencesStore,
                        )
                    }
                    PreferenceKeys.GasToken -> {
                        return@createFrom DydxGasTokenViewModel.currentValueText(
                            preferencesStore = preferencesStore,
                            abacusStateManager = abacusStateManager,
                        )
                    }
                    else -> {
                        return@createFrom ""
                    }
                }
            },
        )
    }
}
