package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.leverage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.maxLeverage
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

private val TAG = "DydxTradeInputLeverageViewModel"

@HiltViewModel
class DydxTradeInputLeverageViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val logger: Logging,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeInputLeverageView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput,
            abacusStateManager.state.tradeInput.mapNotNull { it?.marketId }.flatMapLatest { abacusStateManager.state.market(it) }.map { it.maxLeverage },
            abacusStateManager.state.selectedSubaccountPositions,
        ) { tradeInput, maxLeverage, positions ->
            val marketId = tradeInput?.marketId ?: return@combine null
            val position = positions?.firstOrNull { it.id == marketId }
            createViewState(tradeInput, maxLeverage, position?.leverage?.current)
        }
            .distinctUntilChanged()

    private fun createViewState(
        tradeInput: TradeInput?,
        maxLeverage: Double,
        positionLeverage: Double?
    ): DydxTradeInputLeverageView.ViewState {
        if ((positionLeverage ?: 0.0) > maxLeverage) {
            logger.e(TAG, "Position leverage is greater than max leverage")
        }
        return DydxTradeInputLeverageView.ViewState(
            localizer = localizer,
            formatter = formatter,
            leverage = tradeInput?.size?.leverage ?: 0.0,
            positionLeverage = positionLeverage,
            maxLeverage = maxLeverage,
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
