package exchange.dydx.trading.feature.vault.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.VaultRoutes
import exchange.dydx.trading.feature.vault.VaultInputState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxVaultButtonsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    private val inputState: VaultInputState,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultButtonsView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxVaultButtonsView.ViewState {
        return DydxVaultButtonsView.ViewState(
            localizer = localizer,
            depositAction = {
                inputState.reset()
                router.navigateTo(route = VaultRoutes.deposit, presentation = DydxRouter.Presentation.Modal)
            },
            withdrawAction = {
                inputState.reset()
                router.navigateTo(route = VaultRoutes.withdraw, presentation = DydxRouter.Presentation.Modal)
            },
        )
    }
}
