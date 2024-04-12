package exchange.dydx.dydxstatemanager

import exchange.dydx.abacus.output.PositionSide
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

fun AbacusState.triggerOrders(marketId: String): Flow<List<SubaccountOrder>?> =
    selectedSubaccountOrdersOfMarket(marketId)
        .map { orders ->
            orders?.filter { order ->
                order.status == OrderStatus.untriggered
            }
        }
        .distinctUntilChanged()

fun AbacusState.takeProfitOrders(marketId: String): Flow<List<SubaccountOrder>?> =
    combine(
        selectedSubaccountPositionOfMarket(marketId),
        triggerOrders(marketId),
    ) { position, orders ->
        orders?.filter { order ->
            position?.side?.current?.let { currentSide ->
                (order.type == OrderType.takeProfitMarket || order.type == OrderType.takeProfitLimit) &&
                    order.side.isOppositeOf(currentSide)
            } ?: false
        }
    }
        .distinctUntilChanged()

fun AbacusState.stopLossOrders(marketId: String): Flow<List<SubaccountOrder>?> =
    combine(
        selectedSubaccountPositionOfMarket(marketId),
        triggerOrders(marketId),
    ) { position, orders ->
        orders?.filter { order ->
            position?.side?.current?.let { currentSide ->
                (order.type == OrderType.stopMarket || order.type == OrderType.stopLimit) &&
                    order.side.isOppositeOf(currentSide)
            } ?: false
        }
    }
        .distinctUntilChanged()

private fun OrderSide.isOppositeOf(that: PositionSide): Boolean =
    (this == OrderSide.buy && that == PositionSide.SHORT) || (this == OrderSide.sell && that == PositionSide.LONG)
