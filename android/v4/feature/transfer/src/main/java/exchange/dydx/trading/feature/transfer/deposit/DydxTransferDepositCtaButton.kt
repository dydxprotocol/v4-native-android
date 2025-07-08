package exchange.dydx.trading.feature.transfer.deposit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.InputCtaButton

@Preview
@Composable
fun Preview_DydxTransferDepositCtaButton() {
    DydxThemedPreviewSurface {
        DydxTransferDepositCtaButton.Content(
            Modifier,
            DydxTransferDepositCtaButton.ViewState.preview,
        )
    }
}

object DydxTransferDepositCtaButton : DydxComponent {
    data class ViewState(
        val ctaButton: InputCtaButton.ViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                ctaButton = InputCtaButton.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTransferDepositCtaButtonModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        InputCtaButton.Content(
            modifier = modifier,
            state = state.ctaButton,
        )
    }
}
