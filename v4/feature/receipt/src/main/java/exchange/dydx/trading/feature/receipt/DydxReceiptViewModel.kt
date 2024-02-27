package exchange.dydx.trading.feature.receipt

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.ReceiptLine
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxReceiptViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxReceiptView.ViewState?> =
        abacusStateManager.state.receipts
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(receipts: List<ReceiptLine>): DydxReceiptView.ViewState {
        return DydxReceiptView.ViewState(
            localizer = localizer,
            lineTypes = receipts.mapNotNull { receiptLine ->
                receiptLine.toType()
            },
        )
    }
}

private fun ReceiptLine.toType(): DydxReceiptView.ReceiptLineType? {
    return when (this) {
        ReceiptLine.buyingPower -> DydxReceiptView.ReceiptLineType.BuyingPower
        ReceiptLine.marginUsage -> DydxReceiptView.ReceiptLineType.MarginUsage
        ReceiptLine.fee -> DydxReceiptView.ReceiptLineType.Fee
        ReceiptLine.expectedPrice -> DydxReceiptView.ReceiptLineType.ExpectedPrice
        ReceiptLine.reward -> DydxReceiptView.ReceiptLineType.Rewards
        ReceiptLine.equity -> DydxReceiptView.ReceiptLineType.Equity
        ReceiptLine.exchangeRate -> DydxReceiptView.ReceiptLineType.ExchangeRate
        ReceiptLine.exchangeReceived -> DydxReceiptView.ReceiptLineType.ExchangeReceived
        ReceiptLine.transferRouteEstimatedDuration -> DydxReceiptView.ReceiptLineType.TransferDuration
        ReceiptLine.slippage -> DydxReceiptView.ReceiptLineType.Slippage
        ReceiptLine.bridgeFee -> DydxReceiptView.ReceiptLineType.BridgeFee
        ReceiptLine.gasFee -> DydxReceiptView.ReceiptLineType.GasFee
        else -> null
    }
}
