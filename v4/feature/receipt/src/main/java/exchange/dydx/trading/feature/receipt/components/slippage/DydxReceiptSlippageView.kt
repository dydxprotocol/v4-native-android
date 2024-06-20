package exchange.dydx.trading.feature.receipt.components.slippage

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.feature.receipt.components.DydxReceiptItemView

@Preview
@Composable
fun Preview_DydxReceiptSlippageView() {
    DydxThemedPreviewSurface {
        DydxReceiptSlippageView.Content(Modifier, DydxReceiptItemView.ViewState.preview)
    }
}

object DydxReceiptSlippageView : DydxReceiptItemView(), DydxComponent {
    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxReceiptSlippageViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: DydxReceiptItemView.ViewState?) {
        DydxReceiptItemView.Content(modifier, state)
    }
}
