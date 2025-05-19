package exchange.dydx.trading.feature.receipt.components.fee

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxReceiptGasFeeView() {
    DydxThemedPreviewSurface {
        DydxReceiptGasFeeView.Content(Modifier, DydxReceiptBaseFeeView.ViewState.preview)
    }
}

object DydxReceiptGasFeeView : DydxReceiptBaseFeeView(), DydxComponent {

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxReceiptGasFeeViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        DydxReceiptBaseFeeView.Content(modifier, state)
    }
}
