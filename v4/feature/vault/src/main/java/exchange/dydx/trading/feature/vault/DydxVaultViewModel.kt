package exchange.dydx.trading.feature.vault

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.vault.components.DydxVaultPositionItemView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DydxVaultViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultView.ViewState?> = abacusStateManager.state.marketSummary
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(marketSummary: PerpetualMarketSummary?): DydxVaultView.ViewState {
        return DydxVaultView.ViewState(
            localizer = localizer,
            items = listOf(
                DydxVaultPositionItemView.ViewState.preview.copy(id = UUID.randomUUID().toString(), side = DydxVaultPositionItemView.ViewState.preview.side?.copy(localizer = localizer)),
                DydxVaultPositionItemView.ViewState.preview.copy(id = UUID.randomUUID().toString(), side = DydxVaultPositionItemView.ViewState.preview.side?.copy(localizer = localizer)),
                DydxVaultPositionItemView.ViewState.preview.copy(id = UUID.randomUUID().toString(), side = DydxVaultPositionItemView.ViewState.preview.side?.copy(localizer = localizer)),
                DydxVaultPositionItemView.ViewState.preview.copy(id = UUID.randomUUID().toString(), side = DydxVaultPositionItemView.ViewState.preview.side?.copy(localizer = localizer)),
            ),
        )
    }
}
