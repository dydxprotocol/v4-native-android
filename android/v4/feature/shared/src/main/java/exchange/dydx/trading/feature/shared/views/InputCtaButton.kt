package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer

@Preview
@Composable
fun Preview_InputCtaButton() {
    DydxThemedPreviewSurface {
        InputCtaButton.Content(Modifier, InputCtaButton.ViewState.preview)
    }
}

object InputCtaButton {
    sealed interface State {
        val message: String?
        data class Enabled(override val message: String? = null) : State
        data class Disabled(override val message: String? = null) : State
        data object Thinking : State {
            override val message: String? = null
        }
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
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        PlatformButton(
            modifier = modifier.fillMaxWidth(),
            text = when (state.ctaButtonState) {
                is State.Enabled ->
                    state.ctaButtonState.message
                        ?: state.localizer.localize("APP.TRADE.PREVIEW")
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
