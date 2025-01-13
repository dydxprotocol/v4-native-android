package exchange.dydx.trading.feature.vault.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
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
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.shared.views.SparklineView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import java.util.UUID

@Preview
@Composable
fun Preview_DydxVaultPositionItemView() {
    DydxThemedPreviewSurface {
        DydxVaultPositionItemView.Content(Modifier, DydxVaultPositionItemView.ViewState.preview)
    }
}

object DydxVaultPositionItemView : DydxComponent {
    val marketSectionWidth = 130.dp
    val chartWidth = 38.dp

    data class ViewState(
        val localizer: LocalizerProtocol,
        val id: String = UUID.randomUUID().toString(),
        val logoUrl: String? = null,
        val assetName: String? = null,
        val market: String? = null,
        val side: SideTextView.ViewState? = null,
        val leverage: String? = null,
        val notionalValue: String? = null,
        val positionSize: String? = null,
        val equity: String? = null,
        val token: TokenTextView.ViewState? = null,
        val pnlAmount: SignedAmountView.ViewState? = null,
        val pnlPercentage: String? = null,
        val sparkline: SparklineView.ViewState? = null
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                logoUrl = "https://v4.testnet.dydx.exchange/currencies/eth.png",
                assetName = "Asset Name",
                market = "Market",
                side = SideTextView.ViewState.preview.copy(
                    coloringOption = SideTextView.ColoringOption.COLORED,
                    side = SideTextView.Side.Long,
                ),
                leverage = "2.0x",
                notionalValue = "$100.0",
                positionSize = "200.0",
                token = TokenTextView.ViewState.preview,
                pnlAmount = SignedAmountView.ViewState.preview.copy(
                    sign = PlatformUISign.Plus,
                    coloringOption = SignedAmountView.ColoringOption.AllText,
                ),
                pnlPercentage = "10%",
                sparkline = SparklineView.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultPositionItemViewModel = hiltViewModel()

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
                .padding(vertical = ThemeShapes.VerticalPadding, horizontal = ThemeShapes.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MarketColumnContent(
                modifier = Modifier.width(marketSectionWidth),
                state = state,
            )

            SizeColumnContent(
                modifier = Modifier.weight(1f),
                state = state,
            )

            PnLColumnContent(
                modifier = Modifier,
                state = state,
            )

            Column(
                modifier = Modifier.width(chartWidth),
            ) {
                SparklineView.Content(
                    modifier = Modifier.size(chartWidth, 23.dp),
                    state = state.sparkline,
                )
            }
        }
    }

    @Composable
    fun MarketColumnContent(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.assetName == "USDC") {
                PlatformRoundImage(
                    icon = R.drawable.vault_usdc_token,
                    size = 24.dp,
                )
            } else if (state.logoUrl != null) {
                PlatformRoundImage(
                    icon = state.logoUrl,
                    size = 24.dp,
                )
            } else {
                Canvas(modifier = Modifier.size(24.dp), onDraw = {
                    drawCircle(color = ThemeColor.SemanticColor.layer_5.color)
                })
            }

            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.assetName ?: "-",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (state.side != null) {
                        SideTextView.Content(
                            modifier = Modifier,
                            state = state.side,
                            textStyle = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.mini),
                        )
                    } else {
                        Text(
                            text = "-",
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.mini)
                                .themeColor(ThemeColor.SemanticColor.text_tertiary),
                        )
                    }

                    if (state.leverage != null) {
                        Text(
                            text = "@",
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.mini)
                                .themeColor(ThemeColor.SemanticColor.text_tertiary),
                        )
                        Text(
                            text = state.leverage ?: "-",
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.mini),
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun SizeColumnContent(modifier: Modifier, state: ViewState) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val combinedValues = (state.notionalValue ?: "-") + " / " + (state.equity ?: "-")
                Text(
                    text = combinedValues,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.positionSize ?: "-",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.mini)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )

                TokenTextView.Content(
                    modifier = Modifier,
                    state = state.token,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny),
                )
            }
        }
    }

    @Composable
    fun PnLColumnContent(modifier: Modifier, state: ViewState) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SignedAmountView.Content(
                modifier = Modifier.align(Alignment.End),
                state = state.pnlAmount,
                textStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )

            Text(
                modifier = Modifier.align(Alignment.End),
                text = state.pnlPercentage ?: "-",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }
}
