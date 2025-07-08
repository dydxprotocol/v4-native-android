package exchange.dydx.trading.feature.market.marketinfo.components.trades

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface

@Preview
@Composable
fun Preview_DydxMarketTradeItemView() {
    DydxThemedPreviewSurface {
        DydxMarketTradeItemView.Content(Modifier.height(20.dp), DydxMarketTradeItemView.ViewState.preview)
    }
}

object DydxMarketTradeItemView {
    data class SideState(
        val barColor: Color,
        val textColor: ThemeColor.SemanticColor,
        val text: String,
    )

    data class ViewState(
        val id: String,
        val time: String?,
        val side: SideState,
        val price: String,
        val size: String,
        val percent: Double,
    ) {
        companion object {
            val preview = ViewState(
                id = "1",
                time = "12:12:12",
                side = SideState(
                    barColor = ThemeColor.SemanticColor.positiveColor.color,
                    textColor = ThemeColor.SemanticColor.positiveColor,
                    text = "BUY",
                ),
                price = "$0.01",
                size = "0.01",
                percent = 0.1,
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState) {
        DydxMarketTradeLayout.Content(
            modifier = modifier,
            colume1 = {
                Text(
                    text = state.time ?: "",
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(
                            fontSize = ThemeFont.FontSize.mini,
                            fontType = ThemeFont.FontType.number,
                        ),
                )
            },
            colume2 = {
                Text(
                    text = state.side.text,
                    style = TextStyle.dydxDefault
                        .themeColor(state.side.textColor)
                        .themeFont(
                            fontSize = ThemeFont.FontSize.mini,
                            fontType = ThemeFont.FontType.book,
                        ),
                )
            },
            colume3 = {
                Text(
                    text = state.price,
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary)
                        .themeFont(
                            fontSize = ThemeFont.FontSize.mini,
                            fontType = ThemeFont.FontType.number,
                        ),
                )
            },
            colume4 = {
                Text(
                    text = state.size,
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary)
                        .themeFont(
                            fontSize = ThemeFont.FontSize.mini,
                            fontType = ThemeFont.FontType.number,
                        ),
                )
            },
            background = {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(state.percent.toFloat())
                        .padding(horizontal = 0.dp, vertical = 0.dp)
                        .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        .background(state.side.barColor)
                        .zIndex(0f),
                ) {
                }
            },
        )
    }
}
