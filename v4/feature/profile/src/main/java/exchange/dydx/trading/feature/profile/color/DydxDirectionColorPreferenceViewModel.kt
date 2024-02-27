package exchange.dydx.trading.feature.profile.color

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.AbacusLocalizerProtocol
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.MarketRoutes
import exchange.dydx.trading.feature.shared.PreferenceKeys
import exchange.dydx.trading.feature.shared.views.SettingsView
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class DydxDirectionColorPreferenceViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val preferencesStore: SharedPreferencesStore,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    private val mutableState = MutableStateFlow(createViewState())

    val state: Flow<SettingsView.ViewState?> = mutableState

    private fun createViewState(): SettingsView.ViewState {
        val currentValue = preferencesStore.read(key = PreferenceKeys.DirectionColor, defaultValue = "green_is_up")
        val items: List<SettingsView.ViewState.Item> = listOf(
            SettingsView.ViewState.Item(
                title = localizer.localize("APP.V4.GREEN_IS_UP"),
                value = "green_is_up",
                selected = currentValue == "green_is_up",
            ),
            SettingsView.ViewState.Item(
                title = localizer.localize("APP.V4.RED_IS_UP"),
                value = "red_is_up",
                selected = currentValue == "red_is_up",
            ),
        )

        return SettingsView.ViewState(
            localizer = localizer,
            header = localizer.localize("APP.V4.DIRECTION_COLOR_PREFERENCE"),
            backAction = {
                router.navigateBack()
            },
            sections = listOf(
                SettingsView.ViewState.Section(
                    items = items,
                ),
            ),
            itemAction = { value ->
                preferencesStore.save(value, PreferenceKeys.DirectionColor)
                mutableState.value = createViewState()
                router.tabTo(MarketRoutes.marketList)
            },
        )
    }

    companion object {
        fun currentValueText(
            localizer: AbacusLocalizerProtocol,
            preferencesStore: SharedPreferencesStore,
        ): String? {
            val value = preferencesStore.read(key = PreferenceKeys.DirectionColor, defaultValue = "green_is_up")
            when (value) {
                "green_is_up" -> {
                    return localizer.localize("APP.V4.GREEN_IS_UP")
                }
                "red_is_up" -> {
                    return localizer.localize("APP.V4.RED_IS_UP")
                }
                else -> {
                    return null
                }
            }
        }
    }
}
