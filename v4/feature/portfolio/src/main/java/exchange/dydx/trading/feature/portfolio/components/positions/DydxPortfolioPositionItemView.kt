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
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformIconButton
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer
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
            false,
        )
    }
}

object DydxPortfolioPositionItemView {
    @Composable
    fun Content(
        modifier: Modifier,
        localizer: LocalizerProtocol,
        position: SharedMarketPositionViewState,
        isIsolatedMarketEnabled: Boolean,
        onTapAction: (SharedMarketPositionViewState) -> Unit = {},
        onModifyMarginAction: (SharedMarketPositionViewState) -> Unit = {},
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
                .height((if (isIsolatedMarketEnabled) 148.dp else 48.dp) + ThemeShapes.VerticalPadding * 2)
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
                .clickable { onTapAction(position) }
                .padding(
                    // inner paddings after clipping
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.VerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isIsolatedMarketEnabled) {
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
                            true,
                        )
                        Spacer(modifier = Modifier.weight(1.0f))

                        ComposePNL(
                            modifier = Modifier,
                            localizer,
                            position,
                            true,
                        )
                        Spacer(modifier = Modifier.weight(1.0f))

                        ComposeMargin(
                            modifier = Modifier,
                            localizer,
                            position,
                            true,
                        )
                        ComposeIsolatedMarketEditButton(
                            position,
                        )
                    }
                }
            } else {
                ComposeAssetPosition(
                    position,
                )

                Spacer(modifier = Modifier.weight(1f))

                ComposePricing(
                    localizer,
                    position,
                )

                ComposePNL(
                    modifier = Modifier,
                    localizer,
                    position,
                )
            }
        }
    }

    @Composable
    fun ComposeAssetPosition(
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
    }

    @Composable
    fun ComposePricing(
        localizer: LocalizerProtocol,
        position: SharedMarketPositionViewState,
        forIsolatedMarket: Boolean = false,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = if (forIsolatedMarket) Alignment.Start else Alignment.End,
        ) {
            if (forIsolatedMarket) {
                Text(
                    text = localizer.localize("APP.GENERAL.INDEX_ENTRY"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }
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
    }

    @Composable
    fun ComposePNL(
        modifier: Modifier,
        localizer: LocalizerProtocol,
        position: SharedMarketPositionViewState,
        forIsolatedMarket: Boolean = false,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = if (forIsolatedMarket) Alignment.Start else Alignment.End,
            modifier = Modifier.width(80.dp),
        ) {
            if (forIsolatedMarket) {
                Text(
                    text = localizer.localize("APP.GENERAL.PROFIT_AND_LOSS"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }

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
    }

    @Composable
    fun ComposeMargin(
        modifier: Modifier,
        localizer: LocalizerProtocol,
        position: SharedMarketPositionViewState,
        forIsolatedMarket: Boolean = false,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = if (forIsolatedMarket) Alignment.Start else Alignment.End,
            modifier = Modifier.width(80.dp),
        ) {
            Text(
                text = localizer.localize("APP.GENERAL.MARGIN"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Text(
                /*
                TODO: Get margin from Abacus
                text = position.margin ?: "",
                 */
                text = "$100.00",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_primary),
            )

            Text(
                /*
                TODO: Get margin type from Abacus
                 */
                text = localizer.localize("APP.GENERAL.ISOLATED"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }

    @Composable
    fun ComposeIsolatedMarketEditButton(
        position: SharedMarketPositionViewState,
    ) {
        Column(
            modifier = Modifier
                .width(32.dp)
                .padding(0.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            /*
            TODO: Only render the button if it is an isolated margin position
             */
            Spacer(modifier = Modifier.weight(1.0f))
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
                    painter = painterResource(id = exchange.dydx.trading.common.R.drawable.ic_edit),
                    contentDescription = "",
                    tint = ThemeColor.SemanticColor.text_primary.color,
                )
            }
        }
    }
}
