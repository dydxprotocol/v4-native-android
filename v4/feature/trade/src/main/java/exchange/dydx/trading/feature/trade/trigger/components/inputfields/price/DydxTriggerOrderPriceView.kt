package exchange.dydx.trading.feature.trade.trigger.components.inputfields.price

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
import exchange.dydx.trading.feature.trade.trigger.components.inputfields.DydxTriggerOrderPriceInputType

@Preview
@Composable
fun Preview_DydxTriggerOrderPriceView() {
    DydxThemedPreviewSurface {
        DydxTriggerOrderPriceView.Content(Modifier, DydxTriggerOrderPriceView.ViewState.preview)
    }
}

object DydxTriggerOrderPriceView : DydxComponent {

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
        Content(modifier, DydxTriggerOrderPriceInputType.TakeProfit)
    }

    @Composable
    fun Content(modifier: Modifier, inputType: DydxTriggerOrderPriceInputType) {
        when (inputType) {
            DydxTriggerOrderPriceInputType.TakeProfit -> {
                val viewModel: DydxTriggerOrderTakeProfitPriceViewModel = hiltViewModel()
                val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
                Content(modifier, state)
            }
            DydxTriggerOrderPriceInputType.StopLoss -> {
                val viewModel: DydxTriggerOrderStopLossPriceViewModel = hiltViewModel()
                val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
                Content(modifier, state)
            }
            DydxTriggerOrderPriceInputType.TakeProfitLimit -> {
                val viewModel: DydxTriggerOrderTakeProfitLimitPriceViewModel = hiltViewModel()
                val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
                Content(modifier, state)
            }
            DydxTriggerOrderPriceInputType.StopLossLimit -> {
                val viewModel: DydxTriggerOrderStopLossLimitPriceViewModel = hiltViewModel()
                val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
                Content(modifier, state)
            }
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        InputFieldScaffold(
            modifier = modifier,
            alertState = state.labeledTextInput.alertState,
        ) {
            LabeledTextInput.Content(
                modifier = Modifier,
                state = state.labeledTextInput,
            )
        }
    }
}
