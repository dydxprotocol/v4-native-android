package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.leverage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.TradeInputField
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
        var cappedPositionLeverage = positionLeverage
        if (cappedPositionLeverage != null) {
            val maxLeverage = tradeInput?.options?.maxLeverage ?: 0.0
            if (cappedPositionLeverage < -maxLeverage) {
                cappedPositionLeverage = -maxLeverage
            } else if (cappedPositionLeverage > maxLeverage) {
                cappedPositionLeverage = maxLeverage
            }
        }
        return DydxTradeInputLeverageView.ViewState(
            localizer = localizer,
            formatter = formatter,
            leverage = tradeInput?.size?.leverage ?: 0.0,
            positionLeverage = cappedPositionLeverage,
            maxLeverage = tradeInput?.options?.maxLeverage,
            side = when (tradeInput?.side) {
                OrderSide.Buy -> DydxTradeInputLeverageView.OrderSide.Buy
                OrderSide.Sell -> DydxTradeInputLeverageView.OrderSide.Sell
                else -> DydxTradeInputLeverageView.OrderSide.Buy
            },
            sideToggleAction = { side ->
                val orderSide = when (side) {
                    DydxTradeInputLeverageView.OrderSide.Buy -> OrderSide.Sell
                    DydxTradeInputLeverageView.OrderSide.Sell -> OrderSide.Buy
                }
                abacusStateManager.trade(orderSide.rawValue, TradeInputField.side)
            },
            leverageUpdateAction = { value ->
                abacusStateManager.trade(value, TradeInputField.leverage)
            },
        )
    }
}
