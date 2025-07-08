package exchange.dydx.trading.feature.market.marketinfo.components.paging

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.navigation.DydxAnimation.AnimateFadeInOut
import exchange.dydx.trading.feature.market.marketinfo.components.account.DydxMarketAccountView
import exchange.dydx.trading.feature.market.marketinfo.components.depth.DydxMarketDepthView
import exchange.dydx.trading.feature.market.marketinfo.components.funding.DydxMarketFundingRateView
import exchange.dydx.trading.feature.market.marketinfo.components.orderbook.DydxMarketOrderbookView
import exchange.dydx.trading.feature.market.marketinfo.components.prices.DydxMarketPricesView
import exchange.dydx.trading.feature.market.marketinfo.components.tiles.DydxMarketTilesView
import exchange.dydx.trading.feature.market.marketinfo.components.trades.DydxMarketTradesListView

@Preview
@Composable
fun Preview_DydxMarketInfoPagingView() {
    DydxThemedPreviewSurface {
        DydxMarketInfoPagingView.Content(Modifier, DydxMarketInfoPagingView.ViewState.preview)
    }
}

object DydxMarketInfoPagingView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val selection: DydxMarketTilesView.TileType,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                selection = DydxMarketTilesView.TileType.ORDERBOOK,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketInfoPagingViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        AnimateFadeInOut(
            visible = state.selection == DydxMarketTilesView.TileType.ORDERBOOK,
        ) {
            DydxMarketOrderbookView.Content(modifier)
        }
        AnimateFadeInOut(
            visible = state.selection == DydxMarketTilesView.TileType.ACCOUNT,
        ) {
            DydxMarketAccountView.Content(modifier)
        }
        AnimateFadeInOut(
            visible = state.selection == DydxMarketTilesView.TileType.DEPTH,
        ) {
            DydxMarketDepthView.Content(modifier)
        }
        AnimateFadeInOut(
            visible = state.selection == DydxMarketTilesView.TileType.PRICE,
        ) {
            DydxMarketPricesView.Content(modifier)
        }
        AnimateFadeInOut(
            visible = state.selection == DydxMarketTilesView.TileType.RECENT,
        ) {
            DydxMarketTradesListView.Content(modifier)
        }
        AnimateFadeInOut(
            visible = state.selection == DydxMarketTilesView.TileType.FUNDING,
        ) {
            DydxMarketFundingRateView.Content(modifier)
        }
    }
}
