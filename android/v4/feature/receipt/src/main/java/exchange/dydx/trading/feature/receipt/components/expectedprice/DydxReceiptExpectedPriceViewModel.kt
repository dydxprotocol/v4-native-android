package exchange.dydx.trading.feature.receipt.components.expectedprice

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.input.TradeInputSummary
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.receipt.components.DydxReceiptItemView
import exchange.dydx.trading.feature.receipt.streams.ReceiptStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxReceiptExpectedPriceViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    val receiptStream: ReceiptStreaming,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxReceiptItemView.ViewState?> =
        combine(
            receiptStream.tradeSummaryFlow,
            abacusStateManager.state.marketMap,
        ) { inputPair, marketMap ->
            val tradeSummary = inputPair?.first ?: return@combine null
            val marketId = inputPair.second ?: return@combine null
            val market = marketMap?.get(marketId) ?: return@combine null
            createViewState(tradeSummary, market)
        }
            .distinctUntilChanged()

    private fun createViewState(
        tradeSummary: TradeInputSummary?,
        market: PerpetualMarket,
    ): DydxReceiptItemView.ViewState {
        return DydxReceiptItemView.ViewState(
            localizer = localizer,
            title = localizer.localize("APP.TRADE.EXPECTED_PRICE"),
            value = if (tradeSummary?.price != null) {
                formatter.dollar(
                    tradeSummary.price ?: 0.0,
                    market.configs?.displayTickSizeDecimals ?: 0,
                )
            } else {
                null
            },
        )
    }
}
