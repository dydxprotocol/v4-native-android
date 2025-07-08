package exchange.dydx.trading.feature.vault.tos

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxVaultTosViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultTosView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxVaultTosView.ViewState {
        val operator = abacusStateManager.environment?.megavaultOperatorName ?: "-"
        val vaultLearnMoreUrl = abacusStateManager.environment?.links?.vaultLearnMore
        val operatorLearnMoreUrl = abacusStateManager.environment?.links?.vaultOperatorLearnMore
        return DydxVaultTosView.ViewState(
            localizer = localizer,
            vaultDesc = localizer.localize("APP.VAULTS.VAULT_DESCRIPTION"),
            operatorDesc = localizer.localizeWithParams(
                "APP.VAULTS.VAULT_OPERATOR_DESCRIPTION",
                params = mapOf("OPERATOR_NAME" to operator),
            ),
            operatorLearnMore = localizer.localizeWithParams(
                "APP.VAULTS.LEARN_MORE_ABOUT_OPERATOR",
                params = mapOf("OPERATOR_NAME" to operator),
            ),
            dydxChainLogoUrl = abacusStateManager.environment?.chainLogo,
            vaultAction = {
                if (vaultLearnMoreUrl != null) {
                    router.navigateTo(vaultLearnMoreUrl)
                }
            },
            operatorAction = {
                if (operatorLearnMoreUrl != null) {
                    router.navigateTo(operatorLearnMoreUrl)
                }
            },
            ctaButtonAction = {
                router.navigateBack()
            },
        )
    }
}
