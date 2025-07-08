package exchange.dydx.trading.feature.market.marketinfo.components.trades

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface

@Preview
@Composable
fun Preview_DydxMarketTradesHeaderView() {
    DydxThemedPreviewSurface {
        DydxMarketTradesHeaderView.Content(Modifier, DydxMarketTradesHeaderView.ViewState.preview)
    }
}

object DydxMarketTradesHeaderView {
    data class ViewState(
        val time: String,
        val side: String,
        val price: String,
        val size: String,
    ) {
        companion object {
            val preview = ViewState(
                time = "Time",
                side = "Side",
                price = "Price",
                size = "Size",
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState) {
        DydxMarketTradeLayout.Content(
            modifier = modifier,
            colume1 = {
                Text(
                    text = state.time,
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(
                            fontSize = ThemeFont.FontSize.mini,
                        ),
                )
            },
            colume2 = {
                Text(
                    text = state.side,
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(
                            fontSize = ThemeFont.FontSize.mini,
                        ),
                )
            },
            colume3 = {
                Text(
                    text = state.price,
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(
                            fontSize = ThemeFont.FontSize.mini,
                        ),
                )
            },
            colume4 = {
                Text(
                    text = state.size,
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(
                            fontSize = ThemeFont.FontSize.mini,
                        ),
                )
            },
        )
    }
}
