package exchange.dydx.trading.feature.trade.margin.components.crossreceipt

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxAdjustMarginInputCrossReceiptViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxAdjustMarginInputCrossReceiptView.ViewState?> =
        abacusStateManager.state.adjustMarginInput.filterNotNull()
            .map { adjustMarginInput ->
                createViewState(adjustMarginInput)
            }
            .distinctUntilChanged()

    private fun createViewState(
        adjustMarginInput: AdjustIsolatedMarginInput,
    ): DydxAdjustMarginInputCrossReceiptView.ViewState {
        return DydxAdjustMarginInputCrossReceiptView.ViewState(
            localizer = localizer,
            formatter = formatter,
            freeCollateral = DydxReceiptFreeCollateralView.ViewState(
                localizer = localizer,
                label = localizer.localize("APP.GENERAL.CROSS_FREE_COLLATERAL"),
                before = adjustMarginInput.summary?.crossFreeCollateral?.let {
                    AmountText.ViewState(
                        localizer = localizer,
                        formatter = formatter,
                        amount = it,
                        tickSize = 2,
                    )
                },
                after = adjustMarginInput.summary?.crossFreeCollateralUpdated?.let {
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
                label = localizer.localize("APP.GENERAL.CROSS_MARGIN_USAGE"),
                before = adjustMarginInput.summary?.crossMarginUsage?.let {
                    MarginUsageView.ViewState(
                        localizer = localizer,
                        displayOption = MarginUsageView.DisplayOption.IconAndValue,
                        percent = it,
                    )
                },
                after = adjustMarginInput.summary?.crossMarginUsageUpdated?.let {
                    MarginUsageView.ViewState(
                        localizer = localizer,
                        displayOption = MarginUsageView.DisplayOption.IconAndValue,
                        percent = it,
                    )
                },
            ),
        )
    }
}
