package exchange.dydx.dydxstatemanager

import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.output.account.SubaccountOrder
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
                order.status == OrderStatus.Untriggered
            }
        }
        .distinctUntilChanged()

fun AbacusState.takeProfitOrders(marketId: String, includeLimitOrders: Boolean): Flow<List<SubaccountOrder>?> =
    combine(
        selectedSubaccountPositionOfMarket(marketId),
        triggerOrders(marketId),
    ) { position, orders ->
        orders?.filter { order ->
            position?.side?.current?.let { currentSide ->
                (
                    order.type == OrderType.TakeProfitMarket ||
                        (order.type == OrderType.TakeProfitLimit && includeLimitOrders)
                    ) &&
                    order.side.isOppositeOf(currentSide)
            } ?: false
        }
    }
        .distinctUntilChanged()

fun AbacusState.stopLossOrders(marketId: String, includeLimitOrders: Boolean): Flow<List<SubaccountOrder>?> =
    combine(
        selectedSubaccountPositionOfMarket(marketId),
        triggerOrders(marketId),
    ) { position, orders ->
        orders?.filter { order ->
            position?.side?.current?.let { currentSide ->
                (
                    order.type == OrderType.StopMarket ||
                        (order.type == OrderType.StopLimit && includeLimitOrders)
                    ) &&
                    order.side.isOppositeOf(currentSide)
            } ?: false
        }
    }
        .distinctUntilChanged()

private fun OrderSide.isOppositeOf(that: PositionSide): Boolean =
    (this == OrderSide.Buy && that == PositionSide.SHORT) || (this == OrderSide.Sell && that == PositionSide.LONG)
