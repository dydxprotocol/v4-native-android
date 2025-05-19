package exchange.dydx.trading.feature.trade.tradestatus.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.PlatformDialog
import exchange.dydx.platformui.components.PlatformDialogScaffold
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

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
        val ctaButtonTitle: String = "Try again",
        val ctaButtonState: PlatformButtonState = PlatformButtonState.Secondary,
        val ctaButtonAction: () -> Unit = {},
        val notificationPrimerDialog: PlatformDialog,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                notificationPrimerDialog = PlatformDialog(),
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
            text = state.ctaButtonTitle,
            state = state.ctaButtonState,
        ) {
            state.ctaButtonAction()
        }

        PlatformDialogScaffold(dialog = state.notificationPrimerDialog)
    }
}
