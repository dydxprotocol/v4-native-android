package exchange.dydx.trading.feature.trade.tradeinput.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.trade.tradeinput.DydxTradeInputView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTradeInputOrderbookToggleViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    val orderbookToggleStateFlow: MutableStateFlow<DydxTradeInputView.OrderbookToggleState>,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeInputOrderbookToggleView.ViewState?> =
        orderbookToggleStateFlow
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        orderbookToggleState: DydxTradeInputView.OrderbookToggleState,
    ): DydxTradeInputOrderbookToggleView.ViewState {
        return DydxTradeInputOrderbookToggleView.ViewState(
            localizer = localizer,
            toggleState = orderbookToggleState,
            onToggleAction = { toggleState ->
                orderbookToggleStateFlow.value = toggleState
            },
        )
    }
}
