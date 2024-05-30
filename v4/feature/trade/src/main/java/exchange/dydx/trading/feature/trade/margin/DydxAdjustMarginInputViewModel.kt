package exchange.dydx.trading.feature.trade.margin

import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Subaccount
import exchange.dydx.abacus.output.SubaccountPosition
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.container.Toaster
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.receipt.DydxReceiptView
import exchange.dydx.trading.feature.receipt.components.buyingpower.DydxReceiptFreeCollateralView
import exchange.dydx.trading.feature.receipt.components.liquidationprice.DydxReceiptLiquidationPriceView
import exchange.dydx.trading.feature.receipt.components.marginusage.DydxReceiptMarginUsageView
import exchange.dydx.trading.feature.shared.views.AmountText
import exchange.dydx.trading.feature.shared.views.MarginUsageView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxAdjustMarginInputViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    val toaster: Toaster,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxAdjustMarginInputView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput,
            abacusStateManager.state.selectedSubaccount,
            abacusStateManager.state.selectedSubaccountPositions,
        ) { tradeInput, subaccount, positions ->
            createViewState(tradeInput, subaccount, positions)
        }
            .distinctUntilChanged()

    private fun createViewState(
        tradeInput: TradeInput?,
        subaccount: Subaccount?,
        positions: List<SubaccountPosition>?,
    ): DydxAdjustMarginInputView.ViewState {
        val isolatedMargin = positions?.firstOrNull()

        /*
        Abacus not implemented for adjust margin yet. This is a placeholder.
         */
        return DydxAdjustMarginInputView.ViewState(
            localizer = localizer,
            formatter = formatter,
            direction = DydxAdjustMarginInputView.MarginDirection.Add,
            percentage = 0.5,
            percentageOptions = listOf(
                DydxAdjustMarginInputView.PercentageOption("10%", 0.1),
                DydxAdjustMarginInputView.PercentageOption("20%", 0.2),
                DydxAdjustMarginInputView.PercentageOption("30%", 0.3),
                DydxAdjustMarginInputView.PercentageOption("50%", 0.5),
            ),
            amountText = "500",
            crossMarginReceipt = DydxAdjustMarginInputView.CrossMarginReceipt(
                freeCollateral = DydxReceiptFreeCollateralView.ViewState(
                    localizer = localizer,
                    before = AmountText.ViewState(
                        localizer = localizer,
                        formatter = formatter,
                        amount = subaccount?.freeCollateral?.current,
                        tickSize = 2,
                    ),
                    after = AmountText.ViewState(
                        localizer = localizer,
                        formatter = formatter,
                        amount = subaccount?.freeCollateral?.postOrder,
                        tickSize = 2,
                    ),
                ),
                marginUsage = DydxReceiptMarginUsageView.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    before = MarginUsageView.ViewState(
                        localizer = localizer,
                        displayOption = MarginUsageView.DisplayOption.IconAndValue,
                        percent = subaccount?.marginUsage?.current ?: 0.5,
                    ),
                    after = MarginUsageView.ViewState(
                        localizer = localizer,
                        displayOption = MarginUsageView.DisplayOption.IconAndValue,
                        percent = subaccount?.marginUsage?.current ?: 0.5,
                    ),
                ),
            ),
            isolatedMarginReceipt = DydxAdjustMarginInputView.IsolatedMarginReceipt(
                liquidationPrice = DydxReceiptLiquidationPriceView.ViewState(
                    localizer = localizer,
                    before = AmountText.ViewState(
                        localizer = localizer,
                        formatter = formatter,
                        amount = isolatedMargin?.liquidationPrice?.current,
                        tickSize = 2,
                    ),
                    after = AmountText.ViewState(
                        localizer = localizer,
                        formatter = formatter,
                        amount = isolatedMargin?.liquidationPrice?.postOrder,
                        tickSize = 2,
                    ),
                ),
                receipts = DydxReceiptView.ViewState(
                    localizer = localizer,
                    height = 128.dp,
                    padding = 0.dp,
                    lineTypes = listOf(
                        DydxReceiptView.ReceiptLineType.IsolatedPositionMarginUsage,
                        DydxReceiptView.ReceiptLineType.IsolatedPositionLeverage,
                    ),
                ),
            ),
            error = null,
            marginDirectionAction = { },
            percentageAction = { },
            editAction = { },
            action = { },
            closeAction = {
                router.navigateBack()
            },
        )
    }
}
