package exchange.dydx.trading.feature.receipt.components.fee

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TradeInputSummary
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.receipt.streams.ReceiptStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxReceiptFeeViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val formatter: DydxFormatter,
    private val receiptStream: ReceiptStreaming,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxReceiptBaseFeeView.ViewState?> =
        receiptStream.tradeSummaryFlow
            .map {
                createViewState(it?.first)
            }
            .distinctUntilChanged()

    private fun createViewState(tradeSummary: TradeInputSummary?): DydxReceiptBaseFeeView.ViewState {
        return DydxReceiptBaseFeeView.ViewState(
            localizer = localizer,
            feeType = localizer.localize("APP.TRADE.TAKER"),
            feeFont = if (tradeSummary?.fee == null) {
                null
            } else if ((tradeSummary.fee ?: 0.0) > 0.0) {
                DydxReceiptBaseFeeView.FeeFont.Number(
                    formatter.dollar(tradeSummary.fee ?: 0.0, 2) ?: "",
                )
            } else {
                DydxReceiptBaseFeeView.FeeFont.String(localizer.localize("APP.GENERAL.FREE"))
            },
        )
    }
}
