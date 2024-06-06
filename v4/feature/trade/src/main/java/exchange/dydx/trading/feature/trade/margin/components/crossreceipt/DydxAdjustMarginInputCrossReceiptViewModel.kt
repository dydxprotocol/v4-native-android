package exchange.dydx.trading.feature.trade.margin.components.crossreceipt

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Subaccount
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.receipt.components.buyingpower.DydxReceiptFreeCollateralView
import exchange.dydx.trading.feature.receipt.components.marginusage.DydxReceiptMarginUsageView
import exchange.dydx.trading.feature.shared.views.AmountText
import exchange.dydx.trading.feature.shared.views.MarginUsageView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@HiltViewModel
class DydxAdjustMarginInputCrossReceiptViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxAdjustMarginInputCrossReceiptView.ViewState?> =
        combine(
            abacusStateManager.state.adjustMarginInput.filterNotNull(),
            abacusStateManager.state.selectedSubaccount,
        ) { adjustMarginInput, subaccount ->
            createViewState(adjustMarginInput, subaccount)
        }
            .distinctUntilChanged()

    private fun createViewState(
        adjustMarginInput: AdjustIsolatedMarginInput,
        subaccount: Subaccount?,
    ): DydxAdjustMarginInputCrossReceiptView.ViewState {
        return DydxAdjustMarginInputCrossReceiptView.ViewState(
            localizer = localizer,
            formatter = formatter,
            crossMarginReceipt = DydxAdjustMarginInputCrossReceiptView.CrossMarginReceipt(
                freeCollateral = DydxReceiptFreeCollateralView.ViewState(
                    localizer = localizer,
                    before = subaccount?.freeCollateral?.current?.let {
                        AmountText.ViewState(
                            localizer = localizer,
                            formatter = formatter,
                            amount = it,
                            tickSize = 2,
                        )
                    },
                    after = subaccount?.freeCollateral?.postOrder?.let {
                        AmountText.ViewState(
                            localizer = localizer,
                            formatter = formatter,
                            amount = it,
                            tickSize = 2,
                        )
                    },
                ),
                marginUsage = DydxReceiptMarginUsageView.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    before = subaccount?.marginUsage?.current?.let {
                        MarginUsageView.ViewState(
                            localizer = localizer,
                            displayOption = MarginUsageView.DisplayOption.IconAndValue,
                            percent = it,
                        )
                    },
                    after = subaccount?.marginUsage?.postOrder?.let {
                        MarginUsageView.ViewState(
                            localizer = localizer,
                            displayOption = MarginUsageView.DisplayOption.IconAndValue,
                            percent = it,
                        )
                    },
                ),
            ),
        )
    }
}
