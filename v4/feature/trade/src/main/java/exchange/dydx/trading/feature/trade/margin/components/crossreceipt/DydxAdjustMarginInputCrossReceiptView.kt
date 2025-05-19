package exchange.dydx.trading.feature.trade.margin.components.crossreceipt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.receipt.components.buyingpower.DydxReceiptFreeCollateralView
import exchange.dydx.trading.feature.receipt.components.marginusage.DydxReceiptMarginUsageView

@Preview
@Composable
fun Preview_DydxAdjustMarginInputCrossReceiptView() {
    DydxThemedPreviewSurface {
        DydxAdjustMarginInputCrossReceiptView.Content(
            Modifier,
            DydxAdjustMarginInputCrossReceiptView.ViewState.preview,
        )
    }
}

object DydxAdjustMarginInputCrossReceiptView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val freeCollateral: DydxReceiptFreeCollateralView.ViewState,
        val marginUsage: DydxReceiptMarginUsageView.ViewState,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
                freeCollateral = DydxReceiptFreeCollateralView.ViewState.preview,
                marginUsage = DydxReceiptMarginUsageView.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxAdjustMarginInputCrossReceiptViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DydxReceiptFreeCollateralView.Content(
                modifier = Modifier,
                state = state.freeCollateral,
            )

            DydxReceiptMarginUsageView.Content(
                modifier = Modifier,
                state = state.marginUsage,
            )
        }
    }
}
