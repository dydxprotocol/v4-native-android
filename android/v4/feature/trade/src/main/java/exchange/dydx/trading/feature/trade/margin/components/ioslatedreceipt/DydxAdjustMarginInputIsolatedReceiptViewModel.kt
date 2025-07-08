package exchange.dydx.trading.feature.trade.margin.components.ioslatedreceipt

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.receipt.components.isolatedmargin.DydxReceiptIsolatedPositionMarginUsageView
import exchange.dydx.trading.feature.receipt.components.leverage.DydxReceiptPositionLeverageView
import exchange.dydx.trading.feature.shared.views.LeverageView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxAdjustMarginInputIsolatedReceiptViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxAdjustMarginInputIsolatedReceiptView.ViewState?> =
        abacusStateManager.state.adjustMarginInput.filterNotNull()
            .map { adjustMarginInput ->
                createViewState(adjustMarginInput)
            }
            .distinctUntilChanged()

    private fun createViewState(
        adjustMarginInput: AdjustIsolatedMarginInput,
    ): DydxAdjustMarginInputIsolatedReceiptView.ViewState {
        return DydxAdjustMarginInputIsolatedReceiptView.ViewState(
            localizer = localizer,
            formatter = formatter,
            leverage = DydxReceiptPositionLeverageView.ViewState(
                localizer = localizer,
                before = adjustMarginInput.summary?.positionLeverage?.let {
                    LeverageView.ViewState(
                        localizer = localizer,
                        formatter = formatter,
                        leverage = it,
                    )
                },
                after = adjustMarginInput.summary?.positionLeverageUpdated?.let {
                    LeverageView.ViewState(
                        localizer = localizer,
                        formatter = formatter,
                        leverage = it,
                    )
                },
            ),
            marginUsage = DydxReceiptIsolatedPositionMarginUsageView.ViewState(
                localizer = localizer,
                formatter = formatter,
                before = adjustMarginInput.summary?.positionMargin,
                after = adjustMarginInput.summary?.positionMarginUpdated,
            ),
        )
    }
}
