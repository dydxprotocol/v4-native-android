package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.leverage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTradeInputLeverageViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeInputLeverageView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput,
            abacusStateManager.state.selectedSubaccountPositions,
        ) { tradeInput, positions ->
            val marketId = tradeInput?.marketId ?: return@combine null
            val position = positions?.firstOrNull { it.id == marketId }
            createViewState(tradeInput, position?.leverage?.current)
        }
            .distinctUntilChanged()

    private fun createViewState(
        tradeInput: TradeInput?,
        positionLeverage: Double?
    ): DydxTradeInputLeverageView.ViewState {
        return DydxTradeInputLeverageView.ViewState(
            localizer = localizer,
            formatter = formatter,
            leverage = tradeInput?.size?.leverage ?: 0.0,
            positionLeverage = positionLeverage,
            maxLeverage = tradeInput?.options?.maxLeverage,
            side = when (tradeInput?.side) {
                OrderSide.buy -> DydxTradeInputLeverageView.OrderSide.Buy
                OrderSide.sell -> DydxTradeInputLeverageView.OrderSide.Sell
                else -> DydxTradeInputLeverageView.OrderSide.Buy
            },
            sideToggleAction = { side ->
                val orderSide = when (side) {
                    DydxTradeInputLeverageView.OrderSide.Buy -> OrderSide.sell
                    DydxTradeInputLeverageView.OrderSide.Sell -> OrderSide.buy
                }
                abacusStateManager.trade(orderSide.rawValue, TradeInputField.side)
            },
            leverageUpdateAction = { value ->
                abacusStateManager.trade(value, TradeInputField.leverage)
            },
        )
    }
}
