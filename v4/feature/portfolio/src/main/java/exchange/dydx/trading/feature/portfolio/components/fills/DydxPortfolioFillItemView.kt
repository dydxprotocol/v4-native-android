package exchange.dydx.trading.feature.portfolio.components.fills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.feature.shared.views.IntervalText
import exchange.dydx.trading.feature.shared.views.OrderStatusView
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import exchange.dydx.trading.feature.shared.viewstate.SharedFillViewState

@Preview
@Composable
fun Preview_DydxPortfolioFillItemView() {
    DydxThemedPreviewSurface {
        DydxPortfolioFillItemView.Content(Modifier, SharedFillViewState.preview)
    }
}

object DydxPortfolioFillItemView {

    @Composable
    fun Content(modifier: Modifier, state: SharedFillViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (state.date != null) {
                    IntervalText.Content(
                        modifier = Modifier,
                        state = state.date,
                        textStyle = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_tertiary)
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                } else {
                    Text(
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center,
                        text = "-",
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_tertiary)
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                }
            }

            Box() {
                PlatformRoundImage(
                    icon = state.logoUrl,
                    size = 32.dp,
                )

                OrderStatusView.Content(
                    modifier = Modifier.offset(
                        x = 22.dp,
                        y = (-4).dp,
                    ),
                    state = OrderStatusView.ViewState(
                        localizer = state.localizer,
                        status = OrderStatusView.Status.Green,
                    ),
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = state.type ?: "",
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_tertiary)
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    SideTextView.Content(
                        modifier = Modifier,
                        state = state.sideText,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )

                    Text(
                        text = "@",
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_tertiary)
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )

                    Text(
                        text = state.price ?: "-",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = state.size ?: "-",
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_tertiary)
                            .themeFont(fontSize = ThemeFont.FontSize.mini),
                    )

                    TokenTextView.Content(
                        modifier = Modifier,
                        state = state.token,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini),
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = state.feeLiquidity ?: "-",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini),
                    )

                    Text(
                        text = state.fee ?: "-",
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_tertiary)
                            .themeFont(fontSize = ThemeFont.FontSize.mini),
                    )
                }
            }
        }
    }
}
