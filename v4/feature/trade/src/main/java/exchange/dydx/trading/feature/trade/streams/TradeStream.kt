package exchange.dydx.trading.feature.trade.streams

import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import javax.inject.Inject

interface TradeStreaming {
    val submissionStatus: Flow<AbacusStateManagerProtocol.SubmissionStatus?>
    val lastOrder: Flow<SubaccountOrder?>
}

interface MutableTradeStreaming : TradeStreaming {
    fun submitTrade()
    fun closePosition()
    fun submitTriggerOrders()
}

@ActivityRetainedScoped
class TradeStream @Inject constructor(
    val abacusStateManager: AbacusStateManagerProtocol,
) : MutableTradeStreaming {

    private val streamScope = CoroutineScope(newSingleThreadContext("TradeStream"))

    private var _submissionStatus: MutableStateFlow<AbacusStateManagerProtocol.SubmissionStatus?> =
        MutableStateFlow(null)
    override val submissionStatus: Flow<AbacusStateManagerProtocol.SubmissionStatus?> = _submissionStatus

    override val lastOrder: Flow<SubaccountOrder?> =
        combine(
            submissionStatus,
            abacusStateManager.state.lastOrder,
        ) { submissionStatus, lastOrder ->
            if (submissionStatus is AbacusStateManagerProtocol.SubmissionStatus.Success) {
                if (lastOrder != null) {
                    if (lastOrder.createdAtHeight != null || lastOrder.goodTilBlock != null) {
                        lastOrder
                    } else {
                        null
                    }
                } else {
                    null
                }
            } else {
                null
            }
        }
            .distinctUntilChanged()

    override fun submitTrade() {
        _submissionStatus.update { null }
        streamScope.launch {
            val tradeInput = abacusStateManager.state.tradeInput.first() ?: return@launch

            abacusStateManager.placeOrder { submissionStatus ->
                if (submissionStatus == AbacusStateManagerProtocol.SubmissionStatus.Success) {
                    abacusStateManager.trade(input = null, type = TradeInputField.size)
                }
                _submissionStatus.update { submissionStatus }
            }
        }
    }

    override fun closePosition() {
        _submissionStatus.update { null }
        streamScope.launch {
            val closePositionInput = abacusStateManager.state.closePositionInput.first() ?: return@launch

            abacusStateManager.closePosition { submissionStatus ->
                _submissionStatus.update { submissionStatus }
            }
        }
    }

    override fun submitTriggerOrders() {
        _submissionStatus.update { null }
        streamScope.launch {
            abacusStateManager.submitTriggerOrders { submissionStatus ->
                _submissionStatus.update { submissionStatus }
            }
        }
    }
}
