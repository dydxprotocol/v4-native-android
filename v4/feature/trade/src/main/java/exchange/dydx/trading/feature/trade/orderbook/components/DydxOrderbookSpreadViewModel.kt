package exchange.dydx.trading.feature.trade.orderbook.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.MarketOrderbook
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxOrderbookSpreadViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxOrderbookSpreadView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput.map { it?.marketId },
            abacusStateManager.state.orderbooksMap,
        ) { marketId, orderbooksMap ->
            createViewState(orderbooksMap?.get(marketId))
        }
            .distinctUntilChanged()

    private fun createViewState(orderbook: MarketOrderbook?): DydxOrderbookSpreadView.ViewState {
        return DydxOrderbookSpreadView.ViewState(
            localizer = localizer,
            percent = formatter.percent(orderbook?.spreadPercent, digits = 2),
        )
    }
}
