package exchange.dydx.trading.feature.receipt.components.equity

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.output.input.TransferInputSummary
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.receipt.ReceiptType
import exchange.dydx.trading.feature.receipt.streams.ReceiptStreaming
import exchange.dydx.trading.feature.shared.views.AmountText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class DydxReceiptEquityViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val receiptTypeFlow: Flow<@JvmSuppressWildcards ReceiptType?>,
    private val receiptStream: ReceiptStreaming,
) : ViewModel(), DydxViewModel {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: Flow<DydxReceiptEquityView.ViewState?> =
        receiptTypeFlow
            .flatMapLatest { receiptType ->
                when (receiptType) {
                    is ReceiptType.Trade -> {
                        abacusStateManager.state.selectedSubaccount
                            .map {
                                createTradeViewState(it)
                            }
                    }
                    is ReceiptType.Transfer -> {
                        combine(
                            abacusStateManager.state.selectedSubaccount,
                            abacusStateManager.state.transferInput,
                            receiptStream.transferSummaryFlow,
                        ) { subaccount, transferInput, summary ->
                            createTransferViewState(subaccount, transferInput, summary)
                        }
                    }
                    else -> flowOf()
                }
            }
            .distinctUntilChanged()

    private fun createTradeViewState(
        subaccount: Subaccount?
    ): DydxReceiptEquityView.ViewState {
        return DydxReceiptEquityView.ViewState(
            localizer = localizer,
            before = if (subaccount?.equity?.current != null) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = subaccount.equity?.current,
                    tickSize = 2,
                )
            } else {
                null
            },
            after = if (subaccount?.equity?.postOrder != null) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = subaccount.equity?.postOrder,
                    tickSize = 2,
                )
            } else {
                null
            },
        )
    }

    private fun createTransferViewState(
        subaccount: Subaccount?,
        transferInput: TransferInput?,
        summary: TransferInputSummary?,
    ): DydxReceiptEquityView.ViewState {
        val transferUsdcSize = when (transferInput?.type) {
            TransferType.deposit -> summary?.toAmountUSD
            TransferType.withdrawal, TransferType.transferOut -> summary?.usdcSize
            else -> null
        }
        val currentEquity = subaccount?.equity?.current

        return DydxReceiptEquityView.ViewState(
            localizer = localizer,
            before = if (currentEquity != null) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = currentEquity,
                    tickSize = 2,
                )
            } else {
                null
            },
            after = if (transferUsdcSize != null && currentEquity != null) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = when (transferInput?.type) {
                        TransferType.deposit -> currentEquity + transferUsdcSize
                        TransferType.withdrawal, TransferType.transferOut -> max(0.0, currentEquity - transferUsdcSize)
                        else -> currentEquity
                    },
                    tickSize = 2,
                )
            } else {
                null
            },
        )
    }
}
