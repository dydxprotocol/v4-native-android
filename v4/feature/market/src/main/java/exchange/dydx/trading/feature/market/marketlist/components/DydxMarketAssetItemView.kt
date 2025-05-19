package exchange.dydx.trading.feature.market.marketlist.components

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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformImage
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
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.shared.views.SparklineView
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketViewState
import java.util.UUID

@Preview
@Composable
fun Preview_DydxMarketAssetItemView() {
    DydxThemedPreviewSurface {
        DydxMarketAssetItemView.Content(Modifier, DydxMarketAssetItemView.ViewState.preview)
    }
}

object DydxMarketAssetItemView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val sharedMarketViewState: SharedMarketViewState? = null,
        val onTapAction: (() -> Unit)? = null,
        val toggleFavoriteAction: (() -> Unit)? = null,
    ) {
        val id: String
            get() = sharedMarketViewState?.id ?: UUID.randomUUID().toString()

        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                sharedMarketViewState = SharedMarketViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketAssetItemViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier
                .clickable { state.onTapAction?.invoke() }
                .fillMaxWidth()
                .height(69.dp)
                .padding(start = 8.dp)
                .padding(end = ThemeShapes.HorizontalPadding)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlatformImage(
                modifier = modifier
                    .clickable {
                        state.toggleFavoriteAction?.invoke()
                    }
                    .size(32.dp)
                    .padding(8.dp),
                icon = if (state.sharedMarketViewState?.isFavorite == true) {
                    R.drawable.icon_fav_on
                } else {
                    R.drawable.icon_fav_off
                },
            )

            PlatformRoundImage(
                icon = state.sharedMarketViewState?.logoUrl,
                size = 40.dp,
            )

            Column(
                modifier = Modifier.width(100.dp),
            ) {
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = state.sharedMarketViewState?.tokenSymbol ?: "-",
                        style = TextStyle.dydxDefault
                            .themeFont(
                                fontType = ThemeFont.FontType.plus,
                                fontSize = ThemeFont.FontSize.base,
                            )
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                        maxLines = 1,
                    )
                }

                Text(
                    text = state.sharedMarketViewState?.volume24H ?: "-",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )
            }

            SparklineView.Content(
                modifier = Modifier
                    .width(48.dp)
                    .padding(vertical = 0.dp)
                    .padding(horizontal = 0.dp),
                state = SparklineView.ViewState(
                    sparkline = state.sharedMarketViewState?.sparkline,
                    sign = state.sharedMarketViewState?.priceChangePercent24H?.sign,
                ),
            )

            Column {
                Row(horizontalArrangement = Arrangement.End) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        // modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally),
                        text = state.sharedMarketViewState?.indexPrice ?: "-",
                        style = TextStyle.dydxDefault
                            .themeFont(
                                fontType = ThemeFont.FontType.plus,
                                fontSize = ThemeFont.FontSize.base,
                            )
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                        maxLines = 1,
                    )
                }

                Row(horizontalArrangement = Arrangement.End) {
                    Spacer(modifier = Modifier.weight(1f))
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
