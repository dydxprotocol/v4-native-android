package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.side

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.tabgroups.PlatformTabGroup
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.BuySellView

@Preview
@Composable
fun Preview_DydxTradeInputSideView() {
    DydxThemedPreviewSurface {
        DydxTradeInputSideView.Content(Modifier, DydxTradeInputSideView.ViewState.preview)
    }
}

object DydxTradeInputSideView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val sides: List<String> = listOf(),
        val selectedIndex: Int = 0,
        val color: ThemeColor.SemanticColor = ThemeColor.SemanticColor.positiveColor,
        val onSelectionChanged: (Int) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                sides = listOf("Buy", "Sell"),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeInputSideViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        PlatformTabGroup(
            modifier = modifier.fillMaxWidth(),
            items = state.sides.mapIndexed { index, it ->
                { modifier ->
                    BuySellView.Content(
                        modifier = modifier,
                        state = BuySellView.ViewState(
                            localizer = state.localizer,
                            text = it,
                            color = ThemeColor.SemanticColor.text_tertiary,
                            buttonType = BuySellView.ButtonType.Secondary,
                        ),
                    )
                }
            },
            selectedItems = state.sides.mapIndexed { index, it ->
                { modifier ->
                    BuySellView.Content(
                        modifier = modifier,
                        state = BuySellView.ViewState(
                            localizer = state.localizer,
                            text = it,
                            color = state.color,
                            buttonType = BuySellView.ButtonType.Primary,
                        ),
                    )
                }
            },
            currentSelection = state.selectedIndex,
            onSelectionChanged = state.onSelectionChanged,
            equalWeight = true,
        )
    }
}
