package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.postonly

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.inputs.PlatformSwitchInput
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxTradeInputPostOnlyView() {
    DydxThemedPreviewSurface {
        DydxTradeInputPostOnlyView.Content(Modifier, DydxTradeInputPostOnlyView.ViewState.preview)
    }
}

object DydxTradeInputPostOnlyView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val value: Boolean,
        val onValueChanged: (Boolean) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                value = true,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeInputPostOnlyViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }
        PlatformSwitchInput(
            modifier = modifier.padding(ThemeShapes.InputPaddingValues),
            label = state.localizer?.localize("APP.TRADE.POST_ONLY"),
            value = state.value,
            onValueChange = state.onValueChanged,
        )
    }
}
