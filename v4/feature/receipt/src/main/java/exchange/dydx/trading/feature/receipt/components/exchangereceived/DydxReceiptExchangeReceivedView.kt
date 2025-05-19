package exchange.dydx.trading.feature.receipt.components.exchangereceived

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.receipt.components.DydxReceiptItemView

@Preview
@Composable
fun Preview_DydxReceiptExchangeReceivedView() {
    DydxThemedPreviewSurface {
        DydxReceiptExchangeReceivedView.Content(
            Modifier,
            DydxReceiptItemView.ViewState.preview,
        )
    }
}

object DydxReceiptExchangeReceivedView : DydxReceiptItemView(), DydxComponent {

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxReceiptExchangeReceivedViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        DydxReceiptItemView.Content(modifier, state)
    }
}
