package exchange.dydx.trading.feature.portfolio.components.pendingpositions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxPortfolioPendingPositionView() {
    DydxThemedPreviewSurface {
        DydxPortfolioPendingPositionView.Content(
            Modifier,
            DydxPortfolioPendingPositionView.ViewState.preview
        )
    }
}

object DydxPortfolioPendingPositionView {
    data class ViewState(
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

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlatformRoundImage(
                    icon = state.logoUrl,
                    size = 20.dp,
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = state.marketName ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium)
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
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
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )
            }

            PlatformDivider()


            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.localizer.localize("APP.TRADE.VIEW_ORDER"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.color_purple),
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = state.localizer.localize("APP.TRADE.CANCEL_ORDER"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.color_red),
                )
            }
        }
    }
}

