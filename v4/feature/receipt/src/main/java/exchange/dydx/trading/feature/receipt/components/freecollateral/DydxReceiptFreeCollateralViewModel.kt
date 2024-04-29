package exchange.dydx.trading.feature.receipt.components.buyingpower

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Subaccount
import exchange.dydx.abacus.output.SubaccountPosition
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.receipt.ReceiptType
import exchange.dydx.trading.feature.shared.views.AmountText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxReceiptFreeCollateralViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val receiptTypeFlow: Flow<@JvmSuppressWildcards ReceiptType?>,
) : ViewModel(), DydxViewModel {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: Flow<DydxReceiptFreeCollateralView.ViewState?> =
        receiptTypeFlow
            .flatMapLatest { receiptType ->
                when (receiptType) {
                    is ReceiptType.Trade -> {
                        combine(
                            abacusStateManager.state.selectedSubaccountPositions,
                            abacusStateManager.state.tradeInput,
                        ) { positions, tradeInput ->
                            createViewState(positions, tradeInput)
                        }
                    }
                    is ReceiptType.Transfer -> {
                        combine(
                            abacusStateManager.state.selectedSubaccount,
                            abacusStateManager.state.transferInput,
                        ) { subaccount, transferInput ->
                            createViewState(subaccount, transferInput)
                        }
                    }
                    else -> flowOf()
                }
            }
            .distinctUntilChanged()

    private fun createViewState(
        positions: List<SubaccountPosition>?,
        tradeInput: TradeInput?,
    ): DydxReceiptFreeCollateralView.ViewState {
        val marketId = tradeInput?.marketId ?: "ETH-USD"
        val position = positions?.firstOrNull { it.id == marketId }
        return DydxReceiptFreeCollateralView.ViewState(
            localizer = localizer,
            before = if (position?.freeCollateral?.current != null) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = position.freeCollateral?.current,
                    tickSize = 0,
                    requiresPositive = true,
                )
            } else {
                null
            },
            after = if (position?.freeCollateral?.postOrder != null) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = position.freeCollateral?.postOrder,
                    tickSize = 0,
                    requiresPositive = true,
                )
            } else {
                null
            },
        )
    }

    private fun createViewState(
        subaccount: Subaccount?,
        transferInput: TransferInput?,
    ): DydxReceiptFreeCollateralView.ViewState {
        return DydxReceiptFreeCollateralView.ViewState(
            localizer = localizer,
            before = if (subaccount?.freeCollateral?.current != null) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = subaccount.freeCollateral?.current,
                    tickSize = 0,
                    requiresPositive = true,
                )
            } else {
                null
            },
            after = if (subaccount?.freeCollateral?.postOrder != null) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = subaccount.freeCollateral?.postOrder,
                    tickSize = 0,
                    requiresPositive = true,
                )
            } else {
                null
            },
        )
    }
}
