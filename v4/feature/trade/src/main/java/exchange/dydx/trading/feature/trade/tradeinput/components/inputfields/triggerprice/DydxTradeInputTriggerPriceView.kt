package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.triggerprice

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.scaffolds.InputFieldScaffold
import exchange.dydx.trading.feature.shared.views.LabeledTextInput

@Preview
@Composable
fun Preview_DydxTradeInputTriggerPriceView() {
    DydxThemedPreviewSurface {
        DydxTradeInputTriggerPriceView.Content(
            Modifier,
            DydxTradeInputTriggerPriceView.ViewState.preview,
        )
    }
}

object DydxTradeInputTriggerPriceView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val labeledTextInput: LabeledTextInput.ViewState,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                labeledTextInput = LabeledTextInput.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeInputTriggerPriceViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        InputFieldScaffold(modifier) {
            LabeledTextInput.Content(
                modifier = Modifier,
                state = state.labeledTextInput,
            )
        }
    }
}
