package exchange.dydx.dydxstatemanager

import exchange.dydx.abacus.output.PositionSide
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.SubaccountPosition
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

private val AbacusState.marketIdFlow: Flow<String>
    get() = triggerOrdersInput
        .mapNotNull { it?.marketId }

val AbacusState.triggerOrderPosition: Flow<SubaccountPosition?>
    get() = marketIdFlow
        .flatMapLatest { marketId ->
            selectedSubaccountPositionOfMarket(marketId)
        }
        .filterNotNull()
        .distinctUntilChanged()

val AbacusState.triggerOrders: Flow<List<SubaccountOrder>?>
    get() = marketIdFlow
        .flatMapLatest { marketId ->
            selectedSubaccountOrdersOfMarket(marketId)
        }
        .map { orders ->
            orders?.filter { order ->
                order.status == OrderStatus.untriggered
            }
        }
        .distinctUntilChanged()

val AbacusState.takeProfitOrders: Flow<List<SubaccountOrder>?>
    get() = combine(
        triggerOrderPosition,
        triggerOrders,
    ) { position, orders ->
        orders?.filter { order ->
            position?.side?.current?.let { currentSide ->
                (order.type == OrderType.takeProfitMarket || order.type == OrderType.takeProfitLimit) &&
                    order.side.isOppositeOf(currentSide)
            } ?: false
        }
    }
        .distinctUntilChanged()

val AbacusState.stopLossOrders: Flow<List<SubaccountOrder>?>
    get() = combine(
        triggerOrderPosition,
        triggerOrders,
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
