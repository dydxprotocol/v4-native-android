package exchange.dydx.trading.feature.market.marketinfo.components.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontVariation.weight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformIconButton
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.components.textgroups.PlatformAutoSizingText
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
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketViewState

@Preview
@Composable
fun Preview_DydxMarketInfoHeaderView() {
    DydxThemedPreviewSurface {
        DydxMarketInfoHeaderView.Content(Modifier, DydxMarketInfoHeaderView.ViewState.preview)
    }
}

object DydxMarketInfoHeaderView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val sharedMarketViewState: SharedMarketViewState?,
        val backAction: () -> Unit = {},
        val toggleFavoriteAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                sharedMarketViewState = SharedMarketViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketInfoHeaderViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Row(
            modifier
                .fillMaxWidth()
                .padding(vertical = ThemeShapes.VerticalPadding)
                .padding(end = ThemeShapes.HorizontalPadding)
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                PlatformIconButton(
                    action = { state.backAction.invoke() },
                    backgroundColor = ThemeColor.SemanticColor.transparent,
                    borderColor = ThemeColor.SemanticColor.transparent,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.chevron_left),
                        contentDescription = "",
                        modifier = Modifier.size(24.dp),
                        tint = ThemeColor.SemanticColor.text_primary.color,
                    )
                }
            }

            PlatformRoundImage(
                icon = state.sharedMarketViewState?.logoUrl,
                size = 40.dp,
            )

            Column(
                modifier = Modifier.weight(1f)
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .align(Alignment.CenterVertically),
            ) {
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlatformAutoSizingText(
                        modifier = Modifier,
                        text = state.sharedMarketViewState?.tokenFullName ?: "",
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.medium)
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                    )

                    PlatformImage(
                        modifier = Modifier
                            .clickable {
                                state.toggleFavoriteAction?.invoke()
                            }
                            .size(16.dp),
                        icon = if (state.sharedMarketViewState?.isFavorite == true) {
                            R.drawable.icon_fav_on
                        } else {
                            R.drawable.icon_fav_off
                        },
                    )
                }

                Row {
                    Text(
                        text = state.sharedMarketViewState?.id?.uppercase() ?: "",
                        style = TextStyle.dydxDefault
                            .themeFont(
                                fontSize = ThemeFont.FontSize.mini,
                                fontType = ThemeFont.FontType.plus,
                            )
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                horizontalAlignment = Alignment.End,
            ) {
                Row(horizontalArrangement = Arrangement.End) {
                    Text(
                        text = state.sharedMarketViewState?.indexPrice ?: "-",
                        style = TextStyle.dydxDefault
                            .themeFont(
                                fontSize = ThemeFont.FontSize.medium,
                            )
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                    )
                }

                Row(horizontalArrangement = Arrangement.End) {
                    SignedAmountView.Content(
                        modifier = Modifier,
                        state = state.sharedMarketViewState?.priceChangePercent24H,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                }
            }
        }
    }
}
