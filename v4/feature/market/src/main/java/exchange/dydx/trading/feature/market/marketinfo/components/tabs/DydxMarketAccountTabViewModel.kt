package exchange.dydx.trading.feature.market.marketinfo.components.tabs

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.portfolio.components.placeholder.DydxPortfolioPlaceholderView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DydxMarketAccountTabViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val selectionFlow: MutableStateFlow<DydxMarketAccountTabView.Selection>,
    private val placeholderSelectionFlow: MutableStateFlow<DydxPortfolioPlaceholderView.Selection>,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketAccountTabView.ViewState?> =
        selectionFlow
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(selection: DydxMarketAccountTabView.Selection): DydxMarketAccountTabView.ViewState {
        return DydxMarketAccountTabView.ViewState(
            localizer = localizer,
            currentSelection = selection,
            onSelectionChanged = { selection ->
                selectionFlow.value = selection
                placeholderSelectionFlow.update {
                    when (selection) {
                        DydxMarketAccountTabView.Selection.Position -> DydxPortfolioPlaceholderView.Selection.Positions
                        DydxMarketAccountTabView.Selection.Orders -> DydxPortfolioPlaceholderView.Selection.Orders
                        DydxMarketAccountTabView.Selection.Trades -> DydxPortfolioPlaceholderView.Selection.Trades
                        DydxMarketAccountTabView.Selection.Funding -> DydxPortfolioPlaceholderView.Selection.Funding
                    }
                }
            },
        )
    }
}
