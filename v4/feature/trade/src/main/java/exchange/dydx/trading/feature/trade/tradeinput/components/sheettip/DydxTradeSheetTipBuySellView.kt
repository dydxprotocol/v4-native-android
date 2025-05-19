package exchange.dydx.trading.feature.trade.tradeinput.components.sheettip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.BuySellView

@Preview
@Composable
fun Preview_DydxTradeSheetTipBuySellView() {
    DydxThemedPreviewSurface {
        DydxTradeSheetTipBuySellView.Content(
            Modifier,
            DydxTradeSheetTipBuySellView.ViewState.preview,
        )
    }
}

object DydxTradeSheetTipBuySellView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val sides: List<Pair<String, ThemeColor.SemanticColor>> = listOf(),
        val onSelectionChanged: (Int) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                sides = listOf(
                    "Buy" to ThemeColor.SemanticColor.positiveColor,
                    "Sell" to ThemeColor.SemanticColor.negativeColor,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeSheetTipBuySellViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding)
                .padding(vertical = ThemeShapes.VerticalPadding),
            horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
        ) {
            state.sides.mapIndexed { index, it ->
                BuySellView.Content(
                    modifier = Modifier
                        .clickable {
                            state.onSelectionChanged(index)
                        }
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    state = BuySellView.ViewState(
                        localizer = state.localizer,
                        text = it.first,
                        color = it.second,
                        buttonType = BuySellView.ButtonType.Primary,
                    ),
                )
            }
        }
    }
}
