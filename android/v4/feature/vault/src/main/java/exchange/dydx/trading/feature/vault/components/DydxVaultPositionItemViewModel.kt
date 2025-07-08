package exchange.dydx.trading.feature.vault.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxVaultPositionItemViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultPositionItemView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxVaultPositionItemView.ViewState {
        return DydxVaultPositionItemView.ViewState(
            localizer = localizer,
        )
    }
}
