package exchange.dydx.trading.feature.market.marketinfo.components.tiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformSelectionButton
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.tabgroups.PlatformTabGroup
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
fun Preview_DydxMarketTilesView() {
    DydxThemedPreviewSurface {
        DydxMarketTilesView.Content(Modifier, DydxMarketTilesView.ViewState.preview)
    }
}

object DydxMarketTilesView : DydxComponent {
    enum class TileType {
        ACCOUNT, PRICE, DEPTH, FUNDING, ORDERBOOK, RECENT
    }

    data class Tile(
        val type: TileType,
    ) {
        val stringKey: String
            get() = when (type) {
                TileType.ACCOUNT -> "APP.GENERAL.ACCOUNT"
                TileType.PRICE -> "APP.GENERAL.PRICE_CHART_SHORT"
                TileType.DEPTH -> "APP.GENERAL.DEPTH_CHART_SHORT"
                TileType.FUNDING -> "APP.GENERAL.FUNDING_RATE_CHART_SHORT"
                TileType.ORDERBOOK -> "APP.TRADE.ORDERBOOK_SHORT"
                TileType.RECENT -> "APP.GENERAL.RECENT"
            }

        val icon: Any
            get() = when (type) {
                TileType.ACCOUNT -> R.drawable.icon_market_account
                TileType.PRICE -> R.drawable.icon_market_price
                TileType.DEPTH -> R.drawable.icon_market_depth
                TileType.FUNDING -> R.drawable.icon_market_funding
                TileType.ORDERBOOK -> R.drawable.icon_market_book
                TileType.RECENT -> R.drawable.icon_market_recent
            }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val tiles: List<Tile> = emptyList(),
        val currentSelection: Tile? = null,
        val selectionChangedAction: (Tile) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                tiles = listOf(
                    Tile(TileType.ACCOUNT),
                    Tile(TileType.PRICE),
                    Tile(TileType.DEPTH),
                    Tile(TileType.FUNDING),
                    Tile(TileType.ORDERBOOK),
                    Tile(TileType.RECENT),
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketTilesViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        PlatformTabGroup(
            modifier = modifier,
            items = state.tiles.map { tile ->
                { modifier ->
                    UnselectedItem(
                        modifier = modifier,
                        tile = tile,
                    )
                }
            },
            selectedItems = state.tiles.map { tile ->
                { modifier ->
                    SelectedItem(
                        modifier = modifier,
                        tile = tile,
                        localizer = state.localizer,
                    )
                }
            },
            currentSelection = state.currentSelection?.let { tile ->
                state.tiles.indexOf(tile)
            },
            onSelectionChanged = { index ->
                state.selectionChangedAction(state.tiles[index])
            },
        )
    }

    @Composable
    private fun UnselectedItem(
        modifier: Modifier = Modifier,
        tile: Tile,
    ) {
        PlatformSelectionButton(
            modifier = modifier,
            selected = false,
        ) {
            PlatformImage(
                icon = tile.icon,
                modifier = Modifier
                    .size(22.dp),
                colorFilter = ColorFilter
                    .tint(ThemeColor.SemanticColor.text_tertiary.color),
            )
        }
    }

    @Composable
    private fun SelectedItem(
        modifier: Modifier = Modifier,
        tile: Tile,
        localizer: LocalizerProtocol,
    ) {
        PlatformSelectionButton(
            selected = true,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PlatformImage(
                    icon = tile.icon,
                    modifier = Modifier
                        .size(22.dp),
                    colorFilter = ColorFilter
                        .tint(ThemeColor.SemanticColor.text_primary.color),
                )
                Text(
                    text = localizer.localize(tile.stringKey),
                    modifier = modifier,
                    style = TextStyle.dydxDefault
                        .themeColor(foreground = ThemeColor.SemanticColor.text_primary)
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
            }
        }
    }
}
