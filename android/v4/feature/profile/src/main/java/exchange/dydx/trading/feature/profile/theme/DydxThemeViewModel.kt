package exchange.dydx.trading.feature.profile.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeConfig
import exchange.dydx.platformui.designSystem.theme.ThemeSettings
import exchange.dydx.platformui.designSystem.theme.colorMap
import exchange.dydx.platformui.settings.PlatformUISettings
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.MarketRoutes
import exchange.dydx.trading.feature.shared.PreferenceKeys
import exchange.dydx.trading.feature.shared.views.SettingsView
import exchange.dydx.utilities.utils.Logging
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

private val settingsFile = "settings_theme.json"
private val defaultTheme = "dark"

@HiltViewModel
class DydxThemeViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
    @ApplicationContext val appContext: Context,
    private val preferencesStore: SharedPreferencesStore,
    private val router: DydxRouter,
    private val logger: Logging,
) : ViewModel(), DydxViewModel {

    private val mutableState = MutableStateFlow(createViewState())

    val state: Flow<SettingsView.ViewState?> = mutableState

    private fun createViewState(): SettingsView.ViewState {
        val settings = PlatformUISettings.loadFromAssets(appContext, settingsFile)
        var viewState = SettingsView.ViewState.createFrom(
            settings = settings,
            localizer = localizer,
            header = localizer.localize("APP.V4.SELECT_A_THEME"),
            backAction = {
                router.navigateBack()
            },
            itemAction = { value ->
                preferencesStore.save(value, PreferenceKeys.Theme)
                ThemeSettings.shared.themeConfig.value = ThemeConfig.createFromPreference(appContext, value, logger)
                ThemeSettings.shared.colorMap = mapOf()
                mutableState.value = createViewState()

                router.tabTo(MarketRoutes.marketList)
            },
        )

        val theme = preferencesStore.read(key = PreferenceKeys.Theme, defaultValue = defaultTheme)

        viewState.sections.firstOrNull()?.items?.forEach { item ->
            item.selected = (item.value == theme)
        }
        return viewState
    }

    companion object {
        fun currentValueText(
            localizer: LocalizerProtocol,
            appContext: Context,
            preferencesStore: SharedPreferencesStore,
        ): String? {
            val theme = preferencesStore.read(key = PreferenceKeys.Theme, defaultValue = defaultTheme)

            val settings = PlatformUISettings.loadFromAssets(appContext, settingsFile)
            var viewState = SettingsView.ViewState.createFrom(
                settings = settings,
                localizer = localizer,
                header = localizer.localize("APP.V4.SELECT_A_THEME"),
            )
            return viewState.sections.firstOrNull()?.items?.first { it.value == theme }?.let {
                it.title?.let { title ->
                    return localizer.localize(title)
                }
            }
        }
    }
}
