package exchange.dydx.trading.feature.trade.orderbook.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.MarketOrderbook
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.manager.OrderbookGrouping
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DydxOrderbookGroupViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    private val _state: MutableStateFlow<DydxOrderbookGroupView.ViewState?> = MutableStateFlow(null)

    val state: Flow<DydxOrderbookGroupView.ViewState?> = _state

    init {
        combine(
            abacusStateManager.state.tradeInput.map { it?.marketId },
            abacusStateManager.state.orderbooksMap,
        ) { marketId, orderbooksMap ->
            orderbooksMap?.get(marketId)
        }
            .distinctUntilChanged()
            .onEach { orderbook ->
                _state.value = createViewState(orderbook)
            }
            .launchIn(viewModelScope)
    }

    private fun createViewState(
        orderbook: MarketOrderbook?,
    ): DydxOrderbookGroupView.ViewState {
        val tickSize = "$" + formatter.raw(orderbook?.grouping?.tickSize ?: 0.0)
        return DydxOrderbookGroupView.ViewState(
            localizer = localizer,
            price = tickSize,
            zoomLevel = orderbook?.grouping?.multiplier?.zoomLevel ?: 0,
            onZoomed = { zoomLevel ->
                abacusStateManager.setOrderbookMultiplier(
                    multiplier = OrderbookGrouping.fromZoomLevel(zoomLevel),
                )

                _state.update { state ->
                    state?.copy(
                        zoomLevel = zoomLevel,
                    )
                }
            },
        )
    }
}

private val OrderbookGrouping.zoomLevel: Int
    get() = when (this) {
        OrderbookGrouping.none -> 0
        OrderbookGrouping.x10 -> 1
        OrderbookGrouping.x100 -> 2
        OrderbookGrouping.x1000 -> 3
        else -> 0
    }

private fun OrderbookGrouping.Companion.fromZoomLevel(zoomLevel: Int): OrderbookGrouping {
    return when (zoomLevel) {
        0 -> OrderbookGrouping.none
        1 -> OrderbookGrouping.x10
        2 -> OrderbookGrouping.x100
        3 -> OrderbookGrouping.x1000
        else -> OrderbookGrouping.none
    }
}
