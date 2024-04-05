package exchange.dydx.trading.feature.trade.trigger

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.PositionSide
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.SubaccountPosition
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class DydxTriggerOrderInputViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val formatter: DydxFormatter,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), DydxViewModel {

    private val marketId: String?

    private val marketIdFlow = abacusStateManager.state.triggerOrdersInput
        .mapNotNull { it?.marketId }

    val state: Flow<DydxTriggerOrderInputView.ViewState?> = flowOf(createViewState())

    init {
        marketId = savedStateHandle["marketId"]

        if (marketId == null) {
            router.navigateBack()
        } else {
            abacusStateManager.setMarket(marketId = marketId)
            abacusStateManager.triggerOrders(input = marketId, type = TriggerOrdersInputField.marketId)
        }

        combine(
            marketIdFlow
                .flatMapLatest { marketId ->
                    abacusStateManager.state.selectedSubaccountPositionOfMarket(marketId)
                }
                .filterNotNull()
                .distinctUntilChanged(),
            marketIdFlow
                .flatMapLatest { marketId ->
                    abacusStateManager.state.selectedSubaccountOrdersOfMarket(marketId)
                }
                .distinctUntilChanged(),
            abacusStateManager.state.triggerOrdersInput,
        ) { position, orders, triggerOrdersInput ->
            updateAbacusTriggerOrder(position, orders, triggerOrdersInput)
        }
            .launchIn(viewModelScope)
    }

    private fun createViewState(): DydxTriggerOrderInputView.ViewState {
        return DydxTriggerOrderInputView.ViewState(
            localizer = localizer,
            closeAction = {
                router.navigateBack()
            },
            backHandler = {
                abacusStateManager.resetTriggerOrders()
            },
        )
    }

    private fun updateAbacusTriggerOrder(
        position: SubaccountPosition?,
        orders: List<SubaccountOrder>?,
        triggerOrdersInput: TriggerOrdersInput?,
    ) {
        val takeProfitOrders = orders?.filter { order ->
            position?.side?.current?.let { currentSide ->
                (order.type == OrderType.takeProfitMarket || order.type == OrderType.takeProfitLimit) &&
                    order.side.isOppositeOf(currentSide)
            } ?: false
        }
        val stopLossOrders = orders?.filter { order ->
            position?.side?.current?.let { currentSide ->
                (order.type == OrderType.stopMarket || order.type == OrderType.stopLimit) &&
                    order.side.isOppositeOf(currentSide)
            } ?: false
        }

        var takeProfitOrderSize = 0.0
        if (takeProfitOrders?.size == 1) {
            takeProfitOrders.first()?.let { order ->
                takeProfitOrderSize = order.size
                if (triggerOrdersInput?.takeProfitOrder?.size == null) {
                    abacusStateManager.triggerOrders(
                        formatter.decimalLocaleAgnostic(order.size),
                        TriggerOrdersInputField.takeProfitOrderSize,
                    )
                }
                if (triggerOrdersInput?.takeProfitOrder?.type == null) {
                    abacusStateManager.triggerOrders(
                        order.type.rawValue,
                        TriggerOrdersInputField.takeProfitOrderType,
                    )
                }
                if (triggerOrdersInput?.takeProfitOrder?.price?.triggerPrice == null) {
                    abacusStateManager.triggerOrders(
                        formatter.decimalLocaleAgnostic(order.triggerPrice),
                        TriggerOrdersInputField.takeProfitPrice,
                    )
                }
                if (triggerOrdersInput?.takeProfitOrder?.price?.limitPrice == null) {
                    abacusStateManager.triggerOrders(
                        formatter.decimalLocaleAgnostic(order.price),
                        TriggerOrdersInputField.takeProfitLimitPrice,
                    )
                }
            }
        }

        var stopLossOrderSize = 0.0
        if (stopLossOrders?.size == 1) {
            stopLossOrders.first()?.let { order ->
                stopLossOrderSize = order.size
                if (triggerOrdersInput?.stopLossOrder?.size == null) {
                    abacusStateManager.triggerOrders(
                        formatter.decimalLocaleAgnostic(order.size),
                        TriggerOrdersInputField.stopLossOrderSize,
                    )
                }
                if (triggerOrdersInput?.stopLossOrder?.size == null) {
                    abacusStateManager.triggerOrders(
                        order.type.rawValue,
                        TriggerOrdersInputField.stopLossOrderType,
                    )
                }
                if (triggerOrdersInput?.stopLossOrder?.price?.triggerPrice == null) {
                    abacusStateManager.triggerOrders(
                        formatter.decimalLocaleAgnostic(order.triggerPrice),
                        TriggerOrdersInputField.stopLossPrice,
                    )
                }
                if (triggerOrdersInput?.stopLossOrder?.price?.limitPrice == null) {
                    abacusStateManager.triggerOrders(
                        formatter.decimalLocaleAgnostic(order.price),
                        TriggerOrdersInputField.stopLossLimitPrice,
                    )
                }
            }
        }

        if (takeProfitOrderSize == 0.0 && stopLossOrderSize == 0.0 && triggerOrdersInput?.size == null) {
            abacusStateManager.triggerOrders(
                formatter.decimalLocaleAgnostic(position?.size?.current),
                TriggerOrdersInputField.size,
            )
        }
    }
}

private fun OrderSide.isOppositeOf(that: PositionSide): Boolean =
    (this == OrderSide.buy && that == PositionSide.SHORT) || (this == OrderSide.sell && that == PositionSide.LONG)
