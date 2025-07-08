package exchange.dydx.trading.feature.vault.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Vault
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.VaultRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxVaultTransferButtonViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultTransferButtonView.ViewState?> =
        abacusStateManager.state.vault
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(vault: Vault?): DydxVaultTransferButtonView.ViewState? {
        val history = vault?.account?.vaultTransfers ?: return null
        return DydxVaultTransferButtonView.ViewState(
            localizer = localizer,
            count = history.size,
            onTapAction = {
                router.navigateTo(VaultRoutes.history, presentation = DydxRouter.Presentation.Push)
            },
        )
    }
}
