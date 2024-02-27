package exchange.dydx.trading.feature.trade.tradestatus.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer

@Preview
@Composable
fun Preview_DydxTradeStatusCtaButtonView() {
    DydxThemedPreviewSurface {
        DydxTradeStatusCtaButtonView.Content(
            Modifier,
            DydxTradeStatusCtaButtonView.ViewState.preview,
        )
    }
}

object DydxTradeStatusCtaButtonView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val returnAction: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeStatusCtaButtonViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        PlatformButton(
            modifier = modifier,
            text = state.localizer.localize("APP.TRADE.RETURN_TO_MARKET"),
            state = PlatformButtonState.Secondary,
        ) {
            state.returnAction()
        }
    }
}
