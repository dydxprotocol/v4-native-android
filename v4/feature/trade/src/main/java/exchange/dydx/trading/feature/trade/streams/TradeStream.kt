package exchange.dydx.trading.feature.trade.streams

import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import javax.inject.Inject

interface TradeStreaming {
    val submissionStatus: Flow<AbacusStateManagerProtocol.SubmissionStatus?>
    val lastOrder: Flow<SubaccountOrder?>
}

interface MutableTradeStreaming : TradeStreaming {
    fun submitTrade()
    fun closePosition()
}

@ActivityRetainedScoped
class TradeStream @Inject constructor(
    val abacusStateManager: AbacusStateManagerProtocol,
) : MutableTradeStreaming {

    private var _submissionStatus: MutableStateFlow<AbacusStateManagerProtocol.SubmissionStatus?> =
        MutableStateFlow(null)
    override val submissionStatus: StateFlow<AbacusStateManagerProtocol.SubmissionStatus?> =
        _submissionStatus.asStateFlow()

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
        if (abacusStateManager.state.tradeInput.value != null) {
            _submissionStatus.update { null }

            abacusStateManager.placeOrder { submissionStatus ->
                if (submissionStatus == AbacusStateManagerProtocol.SubmissionStatus.Success) {
                    abacusStateManager.trade(input = null, type = TradeInputField.size)
                }
                _submissionStatus.update { submissionStatus }
            }
        }
    }

    override fun closePosition() {
        if (abacusStateManager.state.closePositionInput.value != null) {
            _submissionStatus.update { null }

            abacusStateManager.closePosition { submissionStatus ->
                _submissionStatus.update { submissionStatus }
            }
        }
    }
}
