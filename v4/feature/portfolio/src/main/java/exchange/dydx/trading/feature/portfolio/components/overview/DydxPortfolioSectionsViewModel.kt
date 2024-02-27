package exchange.dydx.trading.feature.portfolio.components.overview

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
class DydxPortfolioSectionsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val selectionFlow: MutableStateFlow<DydxPortfolioSectionsView.Selection>,
    private val placeholderSelectionFlow: MutableStateFlow<DydxPortfolioPlaceholderView.Selection>,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxPortfolioSectionsView.ViewState?> =
        selectionFlow
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(selection: DydxPortfolioSectionsView.Selection): DydxPortfolioSectionsView.ViewState {
        return DydxPortfolioSectionsView.ViewState(
            localizer = localizer,
            currentSelection = selection,
            onSelectionChanged = { selection ->
                selectionFlow.update { selection }
                placeholderSelectionFlow.update {
                    when (selection) {
                        DydxPortfolioSectionsView.Selection.Positions -> DydxPortfolioPlaceholderView.Selection.Positions
                        DydxPortfolioSectionsView.Selection.Orders -> DydxPortfolioPlaceholderView.Selection.Orders
                        DydxPortfolioSectionsView.Selection.Trades -> DydxPortfolioPlaceholderView.Selection.Trades
                        DydxPortfolioSectionsView.Selection.Funding -> DydxPortfolioPlaceholderView.Selection.Funding
                    }
                }
            },
        )
    }
}
