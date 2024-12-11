package exchange.dydx.trading.feature.profile.language

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.protocols.AbacusLocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.MarketRoutes
import exchange.dydx.trading.common.navigation.ProfileRoutes.language
import exchange.dydx.trading.feature.shared.PreferenceKeys
import exchange.dydx.trading.feature.shared.views.SettingsView
import exchange.dydx.trading.integration.fcm.FCMRegistrar
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class DydxLanguageViewModel @Inject constructor(
    val localizer: AbacusLocalizerProtocol,
    private val preferencesStore: SharedPreferencesStore,
    private val router: DydxRouter,
    private val fcmRegistrar: FCMRegistrar,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    private val filteredLanguages: List<SelectionOption>
        get() {
            return localizer.languages.filter { language ->
                val restrictedLocales = abacusStateManager.environment?.restrictedLocales
                if (restrictedLocales != null) {
                    return@filter !restrictedLocales.contains(language.type)
                } else {
                    return@filter true
                }
            }
        }

    private val mutableState = MutableStateFlow(createViewState())

    val state: Flow<SettingsView.ViewState?> = mutableState

    private fun createViewState(): SettingsView.ViewState {
        val items: List<SettingsView.ViewState.Item> = filteredLanguages.map { language ->
            SettingsView.ViewState.Item(
                title = language.localizedString(localizer = localizer),
                value = language.type,
                selected = language.type == localizer.language,
            )
        }

        return SettingsView.ViewState(
            localizer = localizer,
            header = localizer.localize("APP.ONBOARDING.SELECT_A_LANGUAGE"),
            backAction = {
                router.navigateBack()
            },
            sections = listOf(
                SettingsView.ViewState.Section(
                    items = items,
                ),
            ),
            itemAction = { value ->
                preferencesStore.save(value, PreferenceKeys.Language)
                localizer.language = value
                mutableState.value = createViewState()
                fcmRegistrar.registerToken() // need to pass up language for push notifs to be translated.

                router.tabTo(MarketRoutes.marketList)
            },
        )
    }

    companion object {
        fun currentValueText(
            localizer: AbacusLocalizerProtocol,
            preferencesStore: SharedPreferencesStore,
        ): String? {
            val language = preferencesStore.read(key = PreferenceKeys.Language, defaultValue = "en")
            return localizer.languages.firstOrNull { it.type == language }
                ?.localizedString(localizer = localizer)
        }
    }
}
