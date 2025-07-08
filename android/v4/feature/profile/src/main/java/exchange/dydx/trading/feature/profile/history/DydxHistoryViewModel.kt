package exchange.dydx.trading.feature.profile.history

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.portfolio.components.placeholder.DydxPortfolioPlaceholderView
import exchange.dydx.trading.feature.shared.views.SelectionBar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DydxHistoryViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
    private val placeholderSelectionFlow: MutableStateFlow<DydxPortfolioPlaceholderView.Selection>,
) : ViewModel(), DydxViewModel {

    private val _state: MutableStateFlow<DydxHistoryView.ViewState?> = MutableStateFlow(createViewState())

    val state: Flow<DydxHistoryView.ViewState?> = _state

    private fun createViewState(): DydxHistoryView.ViewState {
        return DydxHistoryView.ViewState(
            localizer = localizer,
            selectionBarViewState = SelectionBar.ViewState(
                localizer = localizer,
                items = listOf(
                    SelectionBar.Item(
                        text = localizer.localize("APP.GENERAL.TRADES"),
                    ),
                    SelectionBar.Item(
                        text = localizer.localize("APP.GENERAL.TRANSFER"),
                    ),
                    SelectionBar.Item(
                        text = localizer.localize("APP.TRADE.FUNDING_PAYMENTS_SHORT"),
                    ),
                ),
                currentSelection = 0,
                onSelectionChanged = {
                    _state.value = _state.value?.copy(
                        selectionBarViewState = _state.value?.selectionBarViewState?.copy(
                            currentSelection = it,
                        ),
                    )
                    when (it) {
                        0 -> {
                            placeholderSelectionFlow.update {
                                DydxPortfolioPlaceholderView.Selection.Trades
                            }
                        }
                        1 -> {
                            placeholderSelectionFlow.update {
                                DydxPortfolioPlaceholderView.Selection.Transfer
                            }
                        }
                        2 -> {
                            placeholderSelectionFlow.update {
                                DydxPortfolioPlaceholderView.Selection.Funding
                            }
                        }
                    }
                },
                style = SelectionBar.Style.Medium,
            ),
            backButtionAction = {
                router.navigateBack()
            },
        )
    }
}
