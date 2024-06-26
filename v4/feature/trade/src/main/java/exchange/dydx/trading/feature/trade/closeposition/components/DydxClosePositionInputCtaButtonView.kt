package exchange.dydx.trading.feature.trade.closeposition.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxClosePositionInputCtaButtonView() {
    DydxThemedPreviewSurface {
        DydxClosePositionInputCtaButtonView.Content(
            Modifier,
            DydxClosePositionInputCtaButtonView.ViewState.preview,
        )
    }
}

object DydxClosePositionInputCtaButtonView : DydxComponent {
    sealed class State {
        data class Enabled(val message: String? = null) : State()
        data class Disabled(val message: String? = null) : State()
        object Thinking : State()
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val ctaButtonState: State = State.Disabled(),
        val ctaAction: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxClosePositionInputCtaButtonViewModel = hiltViewModel()

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
            text = when (state.ctaButtonState) {
                is State.Enabled ->
                    state.ctaButtonState.message
                        ?: state.localizer.localize("APP.TRADE.CLOSE_POSITION")
                is State.Disabled ->
                    state.ctaButtonState.message
                        ?: state.localizer.localize("ERRORS.TRADE_BOX_TITLE.MISSING_TRADE_SIZE")
                is State.Thinking ->
                    state.localizer.localize("APP.V4.CALCULATING")
            },
            state = when (state.ctaButtonState) {
                is State.Enabled -> PlatformButtonState.Primary
                is State.Disabled -> PlatformButtonState.Disabled
                is State.Thinking -> PlatformButtonState.Disabled
            },
        ) {
            state.ctaAction()
        }
    }
}
