package exchange.dydx.trading.feature.receipt.streams

import exchange.dydx.abacus.output.input.TradeInputSummary
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.feature.receipt.ReceiptType
import exchange.dydx.trading.feature.receipt.TradeReceiptType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

interface ReceiptStreaming {
    val tradeSummaryFlow: Flow<Pair<TradeInputSummary?, String?>?>
}

class ReceiptStream @Inject constructor(
    abacusStateManager: AbacusStateManagerProtocol,
    receiptTypeFlow: Flow<@JvmSuppressWildcards ReceiptType?>,
) : ReceiptStreaming {

    override val tradeSummaryFlow: Flow<Pair<TradeInputSummary?, String?>?> =
        combine(
            receiptTypeFlow,
            abacusStateManager.state.tradeInput,
            abacusStateManager.state.closePositionInput,
        ) { receiptType, tradeInput, closePositionInput ->
            when (receiptType) {
                ReceiptType.Trade(TradeReceiptType.Open) -> {
                    Pair(tradeInput?.summary, tradeInput?.marketId)
                }
                ReceiptType.Trade(TradeReceiptType.Close) -> {
                    Pair(closePositionInput?.summary, closePositionInput?.marketId)
                }
                else -> {
                    null
                }
            }
        }
            .distinctUntilChanged()
}
