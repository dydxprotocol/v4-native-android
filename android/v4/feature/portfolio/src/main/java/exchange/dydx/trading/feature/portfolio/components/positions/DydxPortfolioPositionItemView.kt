package exchange.dydx.trading.feature.portfolio.components.positions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.platformui.components.buttons.PlatformIconButton
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
            MockLocalizer(),
            SharedMarketPositionViewState.preview,
        )
    }
}

object DydxPortfolioPositionItemView {
    @Composable
    fun Content(
        modifier: Modifier,
        localizer: LocalizerProtocol,
        position: SharedMarketPositionViewState,
        onTapAction: (SharedMarketPositionViewState) -> Unit = {},
    ) {
        val shape = RoundedCornerShape(10.dp)
        Row(
            modifier = modifier
                .padding(
                    // outer padding first, before width and height
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.VerticalPadding,
                )
                .fillMaxWidth()
                .height(148.dp + ThemeShapes.VerticalPadding * 2)
                .background(
                    brush = position.gradientType.brush(ThemeColor.SemanticColor.layer_3),
                    shape = shape,
                )
                .clip(shape)
                .clickable { onTapAction(position) }
                .padding(
                    // inner paddings after clipping
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.VerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ComposeAssetPosition(
                        position,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ComposePricing(
                        localizer,
                        position,
                    )
                    Spacer(modifier = Modifier.weight(1.0f))

                    ComposePNL(
                        modifier = Modifier,
                        localizer,
                        position,
                    )
                    Spacer(modifier = Modifier.weight(1.0f))

                    ComposeMargin(
                        modifier = Modifier,
                        localizer,
                        position,
                    )

                    ComposeIsolatedMarketEditButton(
                        position,
                    )
                }
            }
        }
    }

    @Composable
    private fun ComposeAssetPosition(
        position: SharedMarketPositionViewState,
    ) {
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
                        .themeFont(
                            fontSize = ThemeFont.FontSize.tiny,
                            fontType = ThemeFont.FontType.plus,
                        ),
                )

                Spacer(modifier = Modifier.weight(1f))

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

            Text(
                text = position.notionalTotal ?: "-",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }

    @Composable
    private fun ComposePricing(
        localizer: LocalizerProtocol,
        position: SharedMarketPositionViewState,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = localizer.localize("APP.GENERAL.LIQ_ORACLE"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
            Text(
                text = position.liquidationPrice ?: localizer.localize("APP.GENERAL.NONE"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_primary),
            )

            Text(
                text = position.oraclePrice ?: "",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }

    @Composable
    private fun ComposePNL(
        modifier: Modifier,
        localizer: LocalizerProtocol,
        position: SharedMarketPositionViewState,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
            modifier = modifier.width(80.dp),
        ) {
            Text(
                text = localizer.localize("APP.GENERAL.PROFIT_AND_LOSS"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            SignedAmountView.Content(
                modifier = Modifier,
                state = position.unrealizedPNLAmount,
                textStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )

            SignedAmountView.Content(
                modifier = Modifier,
                state = position.unrealizedPNLPercent,
                textStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini),
            )
        }
    }

    @Composable
    private fun ComposeMargin(
        modifier: Modifier,
        localizer: LocalizerProtocol,
        position: SharedMarketPositionViewState,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
            modifier = modifier.width(80.dp),
        ) {
            Text(
                text = localizer.localize("APP.GENERAL.MARGIN"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Text(
                text = position.margin ?: "-",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_primary),
            )

            Text(
                text = position.marginMode?.localizedString(localizer) ?: "",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }

    @Composable
    private fun ComposeIsolatedMarketEditButton(
        position: SharedMarketPositionViewState,
    ) {
        Column(
            modifier = Modifier
                .width(32.dp)
                .padding(0.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Spacer(modifier = Modifier.weight(1.0f))
            if (position.marginMode == MarginMode.Isolated) {
                PlatformIconButton(
                    modifier = Modifier
                        .width(32.dp)
                        .height(32.dp),
                    action = {
                        position.onAdjustMarginAction?.invoke()
                    },
                    padding = 0.dp,
                    shape = RoundedCornerShape(4.dp),
                    backgroundColor = ThemeColor.SemanticColor.layer_6,
                    borderColor = ThemeColor.SemanticColor.layer_7,
                ) {
                    Icon(
                        painter = painterResource(id = exchange.dydx.trading.feature.shared.R.drawable.icon_edit),
                        contentDescription = "",
                        tint = ThemeColor.SemanticColor.text_primary.color,
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(32.dp))
            }
        }
    }
}
