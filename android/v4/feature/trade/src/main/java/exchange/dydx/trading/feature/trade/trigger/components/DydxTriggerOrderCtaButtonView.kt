package exchange.dydx.trading.feature.trade.trigger.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxTriggerOrderCtaButtonView() {
    DydxThemedPreviewSurface {
        DydxTriggerOrderCtaButtonView.Content(
            Modifier,
            DydxTriggerOrderCtaButtonView.ViewState.preview,
        )
    }
}

object DydxTriggerOrderCtaButtonView : DydxComponent {
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
        val viewModel: DydxTriggerOrderCtaButtonViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val focusManager = LocalFocusManager.current

        PlatformButton(
            modifier = modifier,
            text = when (state.ctaButtonState) {
                is State.Enabled ->
                    state.ctaButtonState.message
                        ?: state.localizer.localize("APP.TRADE.ADD_TRIGGERS")
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
            focusManager.clearFocus()
            state.ctaAction()
        }
    }
}
