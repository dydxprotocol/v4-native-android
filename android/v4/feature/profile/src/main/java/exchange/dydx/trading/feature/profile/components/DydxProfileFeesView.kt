package exchange.dydx.trading.feature.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
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
fun Preview_DydxProfileFeesView() {
    DydxThemedPreviewSurface {
        DydxProfileFeesView.Content(Modifier, DydxProfileFeesView.ViewState.preview)
    }
}

object DydxProfileFeesView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val tradingVolume: String? = null,
        val takerFeeRate: String? = null,
        val makerFeeRate: String? = null,
        val tapAction: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                tradingVolume = "$120,000",
                takerFeeRate = "0.01%",
                makerFeeRate = "0.01%",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxProfileFeesViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier
                .clickable(onClick = state.tapAction)
                .background(
                    color = ThemeColor.SemanticColor.layer_3.color,
                    shape = RoundedCornerShape(14.dp),
                ),
        ) {
            CreateHeader(
                modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                state = state,
            )

            PlatformDivider()

            CreateContent(
                modifier = Modifier
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(vertical = 16.dp),
                state = state,
            )
        }
    }

    @Composable
    private fun CreateHeader(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = state.localizer.localize("APP.GENERAL.FEES"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small),
                modifier = Modifier.weight(1f),
            )

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Icon(
                    painter = painterResource(id = R.drawable.chevron_right),
                    contentDescription = "",
                    modifier = Modifier.size(16.dp),
                    tint = ThemeColor.SemanticColor.text_secondary.color,
                )
            }
        }
    }

    @Composable
    private fun CreateContent(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = state.localizer.localize("APP.TRADE.TAKER"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )

                Text(
                    text = state.takerFeeRate ?: "-",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = state.localizer.localize("APP.TRADE.MAKER"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )

                Text(
                    text = state.makerFeeRate ?: "-",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )
            }
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = state.localizer.localize("APP.TRADE.VOLUME"),
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )

                    Text(
                        text = state.localizer.localize("APP.GENERAL.TIME_STRINGS.30D"),
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )
                }

                Text(
                    text = state.tradingVolume ?: "-",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )
            }
        }
    }
}
