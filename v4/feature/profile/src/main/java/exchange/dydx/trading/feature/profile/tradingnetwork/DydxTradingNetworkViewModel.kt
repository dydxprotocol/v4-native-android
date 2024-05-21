package exchange.dydx.trading.feature.profile.tradingnetwork

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.MarketRoutes
import exchange.dydx.trading.feature.shared.views.SettingsView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class DydxTradingNetworkViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    private val mutableState = MutableStateFlow(createViewState())

    val state: Flow<SettingsView.ViewState?> = mutableState

    private fun createViewState(): SettingsView.ViewState {
        val items: List<SettingsView.ViewState.Item> = abacusStateManager.availableEnvironments.map { environment ->
            SettingsView.ViewState.Item(
                title = environment.localizedString(localizer = localizer),
                value = environment.type,
                selected = environment.type == abacusStateManager.currentEnvironmentId.value,
            )
        }
        return SettingsView.ViewState(
            localizer = localizer,
            header = localizer.localize("APP.V4.SWITCH_NETWORK"),
            backAction = {
                router.navigateBack()
            },
            sections = listOf(
                SettingsView.ViewState.Section(
                    items = items,
                ),
            ),
            itemAction = { value ->
                abacusStateManager.setEnvironmentId(value)
                mutableState.value = createViewState()

                router.tabTo(MarketRoutes.marketList, false)
            },
        )
    }

    companion object {
        fun currentValueText(
            localizer: LocalizerProtocol,
            abacusStateManager: AbacusStateManagerProtocol
        ): String? {
            val value = abacusStateManager.currentEnvironmentId.value ?: return null
            return abacusStateManager.availableEnvironments.firstOrNull { it.type == value }?.localizedString(localizer = localizer)
        }
    }
}
