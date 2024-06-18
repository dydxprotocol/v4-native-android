package exchange.dydx.trading.feature.portfolio.components.pendingpositions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer

@Preview
@Composable
fun Preview_DydxPortfolioPendingPositionItemView() {
    DydxThemedPreviewSurface {
        DydxPortfolioPendingPositionItemView.Content(
            Modifier,
            DydxPortfolioPendingPositionItemView.ViewState.preview,
        )
    }
}

object DydxPortfolioPendingPositionItemView {
    data class ViewState(
        val id: String,
        val localizer: LocalizerProtocol,
        val logoUrl: String? = null,
        val marketName: String? = null,
        val margin: String? = null,
        val viewOrderAction: () -> Unit = {},
        val cancelOrderAction: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                id = "1",
                logoUrl = "https://media.dydx.exchange/currencies/eth.png",
                marketName = "Ethereum",
                margin = "1000.00",
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val shape = RoundedCornerShape(10.dp)
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .themeColor(ThemeColor.SemanticColor.layer_3),
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = ThemeShapes.HorizontalPadding,
                        vertical = 12.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PlatformRoundImage(
                        icon = state.logoUrl,
                        size = 20.dp,
                    )

                    Text(
                        text = state.marketName ?: "",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.base)
                            .themeColor(ThemeColor.SemanticColor.text_secondary),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
                ) {
                    Text(
                        text = state.localizer.localize("APP.GENERAL.MARGIN"),
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = state.margin ?: "",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small)
                            .themeColor(ThemeColor.SemanticColor.text_secondary),
                    )
                }
            }

            PlatformDivider()

            Row(
                modifier = Modifier,

                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            horizontal = ThemeShapes.HorizontalPadding,
                            vertical = 12.dp,
                        )
                        .clickable(
                            indication = rememberRipple(color = ThemeColor.SemanticColor.layer_3.color),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { state.viewOrderAction() },
                        ),
                    text = state.localizer.localize("APP.GENERAL.VIEW_ORDER"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.mini)
                        .themeColor(ThemeColor.SemanticColor.color_purple),
                )

                Text(
                    modifier = Modifier
                        .weight(1f)
                        .background(ThemeColor.SemanticColor.transparent.color)
                        .padding(
                            horizontal = ThemeShapes.HorizontalPadding,
                            vertical = 12.dp,
                        )
                        .clickable(
                            indication = rememberRipple(color = ThemeColor.SemanticColor.layer_3.color),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { state.cancelOrderAction() },
                        ),
                    text = state.localizer.localize("APP.TRADE.CANCEL_ALL"),
                    textAlign = TextAlign.End,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.mini)
                        .themeColor(ThemeColor.SemanticColor.color_red),
                )
            }
        }
    }
}
