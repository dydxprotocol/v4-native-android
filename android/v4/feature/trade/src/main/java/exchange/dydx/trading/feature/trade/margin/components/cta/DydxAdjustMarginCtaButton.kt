package exchange.dydx.trading.feature.trade.margin.components.cta

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.feature.shared.views.InputCtaButton

@Preview
@Composable
fun Preview_DydxAdjustMarginCtaButton() {
    DydxThemedPreviewSurface {
        DydxAdjustMarginCtaButton.Content(DydxAdjustMarginCtaButton.ViewState.preview, disable = false)
    }
}

object DydxAdjustMarginCtaButton {
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
    fun Content(
        disable: Boolean,
        modifier: Modifier
    ) {
        val viewModel: DydxAdjustMarginCtaButtonViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(state, disable, modifier)
    }

    @Composable
    fun Content(
        state: ViewState?,
        disable: Boolean,
        modifier: Modifier = Modifier
    ) {
        if (state == null) {
            return
        }

        InputCtaButton.Content(
            modifier = modifier,
            state = if (disable) {
                state.ctaButton?.copy(
                    ctaButtonState = InputCtaButton.State.Disabled(
                        message = state.ctaButton.ctaButtonState.message,
                    ),
                )
            } else {
                state.ctaButton
            },
        )
    }
}
