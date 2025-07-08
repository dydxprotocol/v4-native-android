package exchange.dydx.trading.feature.trade.tradeinput.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
fun Preview_DydxTradeInputMarginButtonsView() {
    DydxThemedPreviewSurface {
        DydxTradeInputMarginButtonsView.Content(
            Modifier,
            DydxTradeInputMarginButtonsView.ViewState.preview,
        )
    }
}

object DydxTradeInputMarginButtonsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val isIsolatedMarketSelected: Boolean = true,
        val isolatedMarketTargetLeverageText: String? = null,
        val onMarginType: () -> Unit = {},
        val onTargetLeverage: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                isolatedMarketTargetLeverageText = "10x",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeInputMarginButtonsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlatformButton(
                modifier = if (state.isIsolatedMarketSelected) {
                    Modifier
                        .weight(1f)
                } else {
                    Modifier
                        .fillMaxWidth()
                },
                state = PlatformButtonState.Secondary,
                fitText = true,
                text = state.localizer.localize(
                    if (state.isIsolatedMarketSelected) {
                        "APP.GENERAL.ISOLATED"
                    } else {
                        "APP.GENERAL.CROSS"
                    },
                ),
            ) {
                state.onMarginType()
            }
            if (state.isIsolatedMarketSelected) {
                PlatformButton(
                    modifier = Modifier,
                    state = PlatformButtonState.Secondary,
                    text = state.isolatedMarketTargetLeverageText,
                    fitText = true,
                ) {
                    state.onTargetLeverage()
                }
            }
        }
    }
}
