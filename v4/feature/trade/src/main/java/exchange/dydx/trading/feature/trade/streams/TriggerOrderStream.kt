package exchange.dydx.trading.feature.trade.streams

import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.stopLossOrders
import exchange.dydx.dydxstatemanager.takeProfitOrders
import exchange.dydx.trading.common.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
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
    val isNewTriggerOrder: Flow<Boolean>
}

interface MutableTriggerOrderStreaming : TriggerOrderStreaming {
    fun updateSubmissionStatus(status: AbacusStateManagerProtocol.SubmissionStatus?)
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

    private val includeLimitOrders = abacusStateManager.environment?.featureFlags?.isSlTpLimitOrdersEnabled == true || BuildConfig.DEBUG

    override val isNewTriggerOrder: Flow<Boolean> =
        combine(
            marketIdFlow.flatMapLatest { abacusStateManager.state.takeProfitOrders(it, includeLimitOrders) },
            marketIdFlow.flatMapLatest { abacusStateManager.state.stopLossOrders(it, includeLimitOrders) },
        ) { takeProfitOrders, stopLossOrders ->
            takeProfitOrders.isNullOrEmpty() && stopLossOrders.isNullOrEmpty()
        }
            .distinctUntilChanged()

    override fun updateSubmissionStatus(status: AbacusStateManagerProtocol.SubmissionStatus?) {
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
