package exchange.dydx.trading.feature.trade.tradeinput.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.feature.shared.views.InputCtaButton

@Preview
@Composable
fun Preview_DydxTradeInputCtaButtonView() {
    DydxThemedPreviewSurface {
        DydxTradeInputCtaButtonView.Content(Modifier, DydxTradeInputCtaButtonView.ViewState.preview)
    }
}

object DydxTradeInputCtaButtonView : DydxComponent {

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
        val viewModel: DydxTradeInputCtaButtonViewModel = hiltViewModel()

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
