package exchange.dydx.trading.feature.profile.gastoken

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.manager.GasToken
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.nativeTokenName
import exchange.dydx.dydxstatemanager.usdcTokenName
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
                title = token.displayName(abacusStateManager) ?: token.name,
                value = token.name,
                selected = token.name == currentValue,
            )
        }

        return SettingsView.ViewState(
            localizer = localizer,
            header = localizer.localize("APP.GENERAL.PAY_GAS_WITH"),
            backAction = {
                router.navigateBack()
            },
            sections = listOf(
                SettingsView.ViewState.Section(
                    items = items,
                ),
            ),
            itemAction = { value ->
                if (value != currentValue) {
                    preferencesStore.save(value, PreferenceKeys.GasToken)
                    updateGasToken(value)
                    mutableState.value = createViewState()
                }
                router.navigateBack()
            },
        )
    }

    private fun updateGasToken(tokenName: String) {
        try {
            val token = GasToken.valueOf(tokenName)
            abacusStateManager.setGasToken(token)
        } catch (e: IllegalArgumentException) {
            logger.e(TAG, "Invalid gas token: $tokenName")
            return
        }
    }

    private val currentValue: String?
        get() {
            return preferencesStore.read(PreferenceKeys.GasToken, defaultValue = "USDC")
        }

    companion object {
        fun currentValueText(
            preferencesStore: SharedPreferencesStore,
            abacusStateManager: AbacusStateManagerProtocol,
        ): String? {
            try {
                val tokenName =
                    preferencesStore.read(PreferenceKeys.GasToken, defaultValue = "USDC")
                val token = GasToken.valueOf(tokenName)
                return token.displayName(abacusStateManager)
            } catch (e: IllegalArgumentException) {
                return null
            }
        }
    }
}

private fun GasToken.displayName(abacusStateManager: AbacusStateManagerProtocol): String? {
    return when (this) {
        GasToken.USDC -> abacusStateManager.usdcTokenName
        GasToken.NATIVE -> abacusStateManager.nativeTokenName
    }
}
