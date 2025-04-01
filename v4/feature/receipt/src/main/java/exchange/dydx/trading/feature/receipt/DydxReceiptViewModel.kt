package exchange.dydx.trading.feature.receipt

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.ReceiptLine
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxReceiptViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxReceiptView.ViewState?> =
        combine(
            abacusStateManager.state.receipts,
            abacusStateManager.state.transferInput,
        ) { receipts, transferInput ->
            createViewState(receipts, transferInput)
        }
            .distinctUntilChanged()

    private fun createViewState(
        receipts: List<ReceiptLine>,
        transferInput: TransferInput?,
    ): DydxReceiptView.ViewState {
        val lineTypes = receipts.mapNotNull { receiptLine ->
            receiptLine.toType()
        }.toMutableList()

        if (transferInput?.type == TransferType.deposit) {
            lineTypes.removeIf {
                it == DydxReceiptView.ReceiptLineType.BridgeFee ||
                    it == DydxReceiptView.ReceiptLineType.TransferDuration ||
                    it == DydxReceiptView.ReceiptLineType.Slippage
            }

            // TODO: Remove Equity if not in Simple mode
        }

        return DydxReceiptView.ViewState(
            localizer = localizer,
            lineTypes = lineTypes,
        )
    }
}

private fun ReceiptLine.toType(): DydxReceiptView.ReceiptLineType? {
    return when (this) {
        ReceiptLine.BuyingPower -> DydxReceiptView.ReceiptLineType.BuyingPower
        ReceiptLine.MarginUsage -> DydxReceiptView.ReceiptLineType.MarginUsage
        ReceiptLine.Fee -> DydxReceiptView.ReceiptLineType.Fee
        ReceiptLine.ExpectedPrice -> DydxReceiptView.ReceiptLineType.ExpectedPrice
        ReceiptLine.Reward -> DydxReceiptView.ReceiptLineType.Rewards
        ReceiptLine.Equity -> DydxReceiptView.ReceiptLineType.Equity
        ReceiptLine.ExchangeRate -> DydxReceiptView.ReceiptLineType.ExchangeRate
        ReceiptLine.ExchangeReceived -> DydxReceiptView.ReceiptLineType.ExchangeReceived
        ReceiptLine.TransferRouteEstimatedDuration -> DydxReceiptView.ReceiptLineType.TransferDuration
        ReceiptLine.Slippage -> DydxReceiptView.ReceiptLineType.Slippage
        ReceiptLine.BridgeFee -> DydxReceiptView.ReceiptLineType.BridgeFee
        ReceiptLine.GasFee -> DydxReceiptView.ReceiptLineType.GasFee
        ReceiptLine.PositionMargin -> DydxReceiptView.ReceiptLineType.PositionMargin
        ReceiptLine.PositionLeverage -> DydxReceiptView.ReceiptLineType.PositionLeverage
        ReceiptLine.LiquidationPrice -> DydxReceiptView.ReceiptLineType.LiquidationPrice
        ReceiptLine.TransferFee -> DydxReceiptView.ReceiptLineType.TransferFee
        ReceiptLine.Total -> null
        ReceiptLine.WalletBalance -> null
        ReceiptLine.CrossFreeCollateral -> null
        ReceiptLine.CrossMarginUsage -> null
    }
}
