package exchange.dydx.trading.feature.profile.gastoken

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.manager.GasToken
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.PreferenceKeys
import exchange.dydx.trading.feature.shared.views.SettingsView
import exchange.dydx.utilities.utils.Logging
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val gasTokenValues = listOf(GasToken.USDC, GasToken.NATIVE)
private val TAG = "DydxGasTokenViewModel"

@HiltViewModel
class DydxGasTokenViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
    private val preferencesStore: SharedPreferencesStore,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val logger: Logging,
) : ViewModel(), DydxViewModel {

    private val mutableState = MutableStateFlow(createViewState())

    val state: Flow<SettingsView.ViewState?> = mutableState

    private fun createViewState(): SettingsView.ViewState {
        val items: List<SettingsView.ViewState.Item> = gasTokenValues.map { token ->
            SettingsView.ViewState.Item(
                title = token.name,
                value = token.name,
                selected = token.name == currentValueText(preferencesStore),
            )
        }

        return SettingsView.ViewState(
            localizer = localizer,
            header = localizer.localize("TOKENS.GAS_TOKEN"),
            backAction = {
                router.navigateBack()
            },
            sections = listOf(
                SettingsView.ViewState.Section(
                    items = items,
                ),
            ),
            itemAction = { value ->
                if (value != currentValueText(preferencesStore)) {
                    preferencesStore.save(value, PreferenceKeys.GasToken)
                    updateGasToken(value)
                    mutableState.value = createViewState()
                }
                router.navigateBack()
            },
        )
    }

    private fun updateGasToken(denom: String) {
        try {
            val token = GasToken.valueOf(denom)
            abacusStateManager.setGasToken(token)
        } catch (e: IllegalArgumentException) {
            logger.e(TAG, "Invalid gas token: $denom")
            return
        }
    }

    companion object {
        fun currentValueText(
            preferencesStore: SharedPreferencesStore,
        ): String? {
            return preferencesStore.read(PreferenceKeys.GasToken, defaultValue = "USDC")
        }
    }
}
