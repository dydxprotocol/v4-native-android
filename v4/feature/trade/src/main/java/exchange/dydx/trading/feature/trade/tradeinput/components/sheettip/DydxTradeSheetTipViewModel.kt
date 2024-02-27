package exchange.dydx.trading.feature.trade.tradeinput.components.sheettip

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTradeSheetTipViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeSheetTipView.ViewState?> =
        abacusStateManager.state.tradeInput
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(tradeInput: TradeInput?): DydxTradeSheetTipView.ViewState {
        val size = tradeInput?.size?.size ?: 0.0
        return DydxTradeSheetTipView.ViewState(
            localizer = localizer,
            tipState = if (size > 0.0) {
                DydxTradeSheetTipView.TipState.Draft
            } else {
                DydxTradeSheetTipView.TipState.BuySell
            },
        )
    }
}
