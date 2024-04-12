package exchange.dydx.trading.feature.trade.streams

import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.output.PositionSide
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.SubaccountPosition
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class GainLossDisplayType {
    Amount,
    Percent;

    val value: String
        get() = when (this) {
            Amount -> "$"
            Percent -> "%"
        }

    companion object {
        val list = listOf(Amount, Percent)
    }
}

interface TriggerOrderStreaming {
    val submissionStatus: Flow<AbacusStateManagerProtocol.SubmissionStatus?>
    val takeProfitGainLossDisplayType: Flow<GainLossDisplayType>
    val stopLossGainLossDisplayType: Flow<GainLossDisplayType>
    val selectedSubaccountPosition: Flow<SubaccountPosition?>
    val selectSubaccountOrders: Flow<List<SubaccountOrder>?>
    val takeProfitOrders: Flow<List<SubaccountOrder>?>
    val stopLossOrders: Flow<List<SubaccountOrder>?>
    val isNewTriggerOrder: Flow<Boolean>
}

interface MutableTriggerOrderStreaming : TriggerOrderStreaming {
    fun updatesubmissionStatus(status: AbacusStateManagerProtocol.SubmissionStatus?)
    fun clearSubmissionStatus()
    fun setTakeProfitGainLossDisplayType(displayType: GainLossDisplayType)
    fun setStopLossGainLossDisplayType(displayType: GainLossDisplayType)
}

@ActivityRetainedScoped
class TriggerOrderStream @Inject constructor(
    val abacusStateManager: AbacusStateManagerProtocol,
) : MutableTriggerOrderStreaming {

    private val _submissionStatus: MutableStateFlow<AbacusStateManagerProtocol.SubmissionStatus?> = MutableStateFlow(null)
    private val _takeProfitGainLossDisplayType = MutableStateFlow(GainLossDisplayType.Amount)
    private val _stopLossGainLossDisplayType = MutableStateFlow(GainLossDisplayType.Amount)

    override val submissionStatus = _submissionStatus
    override val takeProfitGainLossDisplayType = _takeProfitGainLossDisplayType
    override val stopLossGainLossDisplayType = _stopLossGainLossDisplayType

    private val marketIdFlow = abacusStateManager.state.triggerOrdersInput
        .mapNotNull { it?.marketId }

    override val selectedSubaccountPosition: Flow<SubaccountPosition?> =
        marketIdFlow
            .flatMapLatest { marketId ->
                abacusStateManager.state.selectedSubaccountPositionOfMarket(marketId)
            }
            .filterNotNull()
            .distinctUntilChanged()

    override val selectSubaccountOrders: Flow<List<SubaccountOrder>?> =
        marketIdFlow
            .flatMapLatest { marketId ->
                abacusStateManager.state.selectedSubaccountOrdersOfMarket(marketId)
            }
            .map { orders ->
                orders?.filter { order ->
                    order.status != OrderStatus.canceling &&
                        order.status != OrderStatus.cancelled &&
                        order.status != OrderStatus.filled
                }
            }
            .distinctUntilChanged()

    override val takeProfitOrders: Flow<List<SubaccountOrder>?> =
        combine(
            selectedSubaccountPosition,
            selectSubaccountOrders,
        ) { position, orders ->
            orders?.filter { order ->
                position?.side?.current?.let { currentSide ->
                    (order.type == OrderType.takeProfitMarket || order.type == OrderType.takeProfitLimit) &&
                        order.side.isOppositeOf(currentSide)
                } ?: false
            }
        }

    override val stopLossOrders: Flow<List<SubaccountOrder>?> =
        combine(
            selectedSubaccountPosition,
            selectSubaccountOrders,
        ) { position, orders ->
            orders?.filter { order ->
                position?.side?.current?.let { currentSide ->
                    (order.type == OrderType.stopMarket || order.type == OrderType.stopLimit) &&
                        order.side.isOppositeOf(currentSide)
                } ?: false
            }
        }

    override val isNewTriggerOrder: Flow<Boolean> =
        combine(
            takeProfitOrders,
            stopLossOrders,
        ) { takeProfitOrders, stopLossOrders ->
            takeProfitOrders.isNullOrEmpty() && stopLossOrders.isNullOrEmpty()
        }

    override fun updatesubmissionStatus(status: AbacusStateManagerProtocol.SubmissionStatus?) {
        _submissionStatus.update { status }
    }

    override fun clearSubmissionStatus() {
        _submissionStatus.update { null }
        _takeProfitGainLossDisplayType.update { GainLossDisplayType.Amount }
        _stopLossGainLossDisplayType.update { GainLossDisplayType.Amount }
    }

    override fun setTakeProfitGainLossDisplayType(displayType: GainLossDisplayType) {
        _takeProfitGainLossDisplayType.update { displayType }
    }

    override fun setStopLossGainLossDisplayType(displayType: GainLossDisplayType) {
        _stopLossGainLossDisplayType.update { displayType }
    }
}

private fun OrderSide.isOppositeOf(that: PositionSide): Boolean =
    (this == OrderSide.buy && that == PositionSide.SHORT) || (this == OrderSide.sell && that == PositionSide.LONG)
