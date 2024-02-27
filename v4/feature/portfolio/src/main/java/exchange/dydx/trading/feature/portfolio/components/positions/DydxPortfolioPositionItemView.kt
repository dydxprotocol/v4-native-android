package exchange.dydx.trading.feature.portfolio.components.positions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketPositionViewState

@Preview
@Composable
fun Preview_DydxPortfolioPositionItemView() {
    DydxThemedPreviewSurface {
        DydxPortfolioPositionItemView.Content(
            Modifier,
            SharedMarketPositionViewState.preview,
        )
    }
}

object DydxPortfolioPositionItemView {

    @Composable
    fun Content(
        modifier: Modifier,
        position: SharedMarketPositionViewState,
        onTapAction: (SharedMarketPositionViewState) -> Unit = {},
    ) {
        val shape = RoundedCornerShape(10.dp)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding)
                .height(64.dp)
                .background(
                    brush = position.gradientType.brush(ThemeColor.SemanticColor.layer_3),
                    shape = shape,
                )
                .border(
                    width = 1.dp,
                    color = position.gradientType.color.color.copy(alpha = 0.1f),
                    shape = shape,
                )
                .clip(shape)
                .clickable { onTapAction(position) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Spacer(modifier = Modifier.width(8.dp))

            PlatformRoundImage(
                icon = position.logoUrl,
                size = 36.dp,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = position.size ?: "-",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small)
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    TokenTextView.Content(
                        modifier = Modifier,
                        state = position.token,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.tiny, fontType = ThemeFont.FontType.plus),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SideTextView.Content(
                        modifier = Modifier,
                        state = position.side?.copy(
                            coloringOption = SideTextView.ColoringOption.COLORED,

                        ),
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini)
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                    )

                    Text(
                        text = " @ ",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )

                    Text(
                        text = position.leverage ?: "-",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini)
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = position.oraclePrice ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                Text(
                    text = position.entryPrice ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.mini)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(80.dp),
            ) {
                SignedAmountView.Content(
                    modifier = modifier,
                    state = position.unrealizedPNLPercent,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )

                SignedAmountView.Content(
                    modifier = modifier,
                    state = position.unrealizedPNLAmount,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.mini),
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
