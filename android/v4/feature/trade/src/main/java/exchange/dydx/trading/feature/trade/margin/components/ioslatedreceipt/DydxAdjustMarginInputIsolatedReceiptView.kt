package exchange.dydx.trading.feature.trade.margin.components.ioslatedreceipt

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
import exchange.dydx.trading.feature.receipt.components.isolatedmargin.DydxReceiptIsolatedPositionMarginUsageView
import exchange.dydx.trading.feature.receipt.components.leverage.DydxReceiptPositionLeverageView

@Preview
@Composable
fun Preview_DydxAdjustMarginInputIsolatedReceiptView() {
    DydxThemedPreviewSurface {
        DydxAdjustMarginInputIsolatedReceiptView.Content(
            Modifier,
            DydxAdjustMarginInputIsolatedReceiptView.ViewState.preview,
        )
    }
}

object DydxAdjustMarginInputIsolatedReceiptView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val marginUsage: DydxReceiptIsolatedPositionMarginUsageView.ViewState,
        val leverage: DydxReceiptPositionLeverageView.ViewState,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
                marginUsage = DydxReceiptIsolatedPositionMarginUsageView.ViewState.preview,
                leverage = DydxReceiptPositionLeverageView.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxAdjustMarginInputIsolatedReceiptViewModel = hiltViewModel()

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
            DydxReceiptIsolatedPositionMarginUsageView.Content(
                modifier = Modifier,
                state = state.marginUsage,
            )

            DydxReceiptPositionLeverageView.Content(
                modifier = Modifier,
                state = state.leverage,
            )
        }
    }
}
