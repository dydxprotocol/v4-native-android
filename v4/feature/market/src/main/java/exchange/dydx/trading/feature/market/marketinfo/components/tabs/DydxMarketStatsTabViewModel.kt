package exchange.dydx.trading.feature.market.marketinfo.components.tabs

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxMarketStatsTabViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val selectionFlow: MutableStateFlow<DydxMarketStatsTabView.Selection>,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketStatsTabView.ViewState?> = selectionFlow
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(selection: DydxMarketStatsTabView.Selection): DydxMarketStatsTabView.ViewState {
        return DydxMarketStatsTabView.ViewState(
            localizer = localizer,
            currentSelection = selection,
            onSelectionChanged = { selection ->
                selectionFlow.value = selection
            },
        )
    }
}
