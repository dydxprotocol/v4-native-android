package exchange.dydx.trading.feature.trade.closeposition.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
fun Preview_DydxClosePositionInputSizeView() {
    DydxThemedPreviewSurface {
        DydxClosePositionInputSizeView.Content(
            Modifier,
            DydxClosePositionInputSizeView.ViewState.preview,
        )
    }
}

object DydxClosePositionInputSizeView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val token: String? = null,
        val size: String? = null,
        val placeholder: String? = null,
        val onSizeChanged: (String) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                token = "WETH",
                size = "1.0",
                placeholder = "0.0",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxClosePositionInputSizeViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        InputFieldScaffold(modifier) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LabeledTextInput.Content(
                        modifier = Modifier.weight(1f),
                        state = LabeledTextInput.ViewState(
                            localizer = state.localizer,
                            label = state.localizer?.localize("APP.GENERAL.AMOUNT"),
                            token = state.token,
                            value = state.size,
                            placeholder = state.placeholder ?: "",
                            onValueChanged = {
                                state.onSizeChanged(it)
                            },
                        ),
                    )
                }
            }
        }
    }
}
