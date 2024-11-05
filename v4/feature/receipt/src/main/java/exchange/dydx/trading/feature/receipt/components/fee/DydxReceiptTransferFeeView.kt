package exchange.dydx.trading.feature.receipt.components.fee

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.receipt.components.fee.DydxReceiptBaseFeeView.ViewState

@Preview
@Composable
fun Preview_DydxReceiptTransferFeeView() {
    DydxThemedPreviewSurface {
        DydxReceiptTransferFeeView.Content(Modifier, ViewState.preview)
    }
}

object DydxReceiptTransferFeeView : DydxReceiptBaseFeeView(), DydxComponent {

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxReceiptTransferFeeViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        DydxReceiptBaseFeeView.Content(modifier, state)
    }
}
