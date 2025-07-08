package exchange.dydx.trading.feature.market.marketlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxMarketSummaryView() {
    DydxThemedPreviewSurface {
        DydxMarketSummaryView.Content(Modifier, DydxMarketSummaryView.ViewState.preview)
    }
}

object DydxMarketSummaryView : DydxComponent {

    data class SummaryItem(val header: String, val value: String?)

    data class ViewState(
        val localizer: LocalizerProtocol,
        val items: List<SummaryItem>,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                items = listOf(
                    SummaryItem("24h Volume", "1.0M"),
                    SummaryItem("Open Interest", "1.0M"),
                    SummaryItem("Trades", "1.0M"),
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketSummaryViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            state.items.forEachIndexed { index, it ->
                Column(
                    modifier = Modifier
                        .background(
                            color = ThemeColor.SemanticColor.layer_3.color,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .weight(1f)
                        .padding(8.dp),
                ) {
                    Text(
                        text = it.header,
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini)
                            .themeColor(ThemeColor.SemanticColor.text_secondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it.value ?: "",
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (index < state.items.size - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}
