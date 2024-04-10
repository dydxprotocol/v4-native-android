package exchange.dydx.trading.feature.trade.streams

import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.stopLossOrders
import exchange.dydx.dydxstatemanager.takeProfitOrders
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val isNewTriggerOrder: Flow<Boolean>
}

interface MutableTriggerOrderStreaming : TriggerOrderStreaming {
    fun setMarketId(marketId: String)
    fun submitTriggerOrders()
    fun clearSubmissionStatus()
    fun setTakeProfitGainLossDisplayType(displayType: GainLossDisplayType)
    fun setStopLossGainLossDisplayType(displayType: GainLossDisplayType)
}

@ActivityRetainedScoped
class TriggerOrderStream @Inject constructor(
    val abacusStateManager: AbacusStateManagerProtocol,
) : MutableTriggerOrderStreaming {
    override val submissionStatus get() = _submissionStatus
    override val takeProfitGainLossDisplayType get() = _takeProfitGainLossDisplayType
    override val stopLossGainLossDisplayType get() = _stopLossGainLossDisplayType

    private val _submissionStatus: MutableStateFlow<AbacusStateManagerProtocol.SubmissionStatus?> = MutableStateFlow(null)
    private val _takeProfitGainLossDisplayType = MutableStateFlow(GainLossDisplayType.Amount)
    private val _stopLossGainLossDisplayType = MutableStateFlow(GainLossDisplayType.Amount)

    private val streamScope = MainScope()

    override val isNewTriggerOrder: Flow<Boolean> =
        combine(
            abacusStateManager.state.takeProfitOrders,
            abacusStateManager.state.stopLossOrders,
        ) { takeProfitOrders, stopLossOrders ->
            takeProfitOrders.isNullOrEmpty() && stopLossOrders.isNullOrEmpty()
        }

    override fun setMarketId(marketId: String) {
        abacusStateManager.setMarket(marketId = marketId)
        abacusStateManager.triggerOrders(input = marketId, type = TriggerOrdersInputField.marketId)
    }

    override fun submitTriggerOrders() {
        _submissionStatus.update { null }
        streamScope.launch {
            abacusStateManager.commitTriggerOrders { submissionStatus ->
                _submissionStatus.update { submissionStatus }
            }
        }
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
