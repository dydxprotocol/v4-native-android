package exchange.dydx.trading.feature.market.marketlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxPredictionMarketBannerView() {
    DydxThemedPreviewSurface {
        DydxPredictionMarketBannerView.Content(
            Modifier,
            DydxPredictionMarketBannerView.ViewState.preview,
        )
    }
}

object DydxPredictionMarketBannerView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val onTapAction: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxPredictionMarketBannerViewModel = hiltViewModel()

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
                .background(
                    color = ThemeColor.SemanticColor.layer_3.color,
                    shape = RoundedCornerShape(8.dp),
                )
                .clickable {
                    state.onTapAction()
                }
                .padding(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "🇺🇸",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.large)
                    .themeColor(foreground = ThemeColor.SemanticColor.text_primary),
            )

            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.localizer.localize("APP.PREDICTION_MARKET.LEVERAGE_TRADE_US_ELECTION_SHORT"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.base)
                        .themeColor(foreground = ThemeColor.SemanticColor.text_primary),
                )
                Text(
                    text = state.localizer.localize("APP.PREDICTION_MARKET.WITH_PREDICTION_MARKETS"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(foreground = ThemeColor.SemanticColor.text_secondary),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PlatformRoundImage(
                modifier = Modifier
                    .background(
                        color = ThemeColor.SemanticColor.layer_5.color,
                        shape = CircleShape,
                    )
                    .size(28.dp),
                icon = R.drawable.icon_right_arrow_2,
            )
        }
    }
}
