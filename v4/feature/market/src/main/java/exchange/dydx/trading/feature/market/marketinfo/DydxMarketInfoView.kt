package exchange.dydx.trading.feature.market.marketinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.compose.PlatformRememberLazyListState
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.market.marketinfo.components.configs.DydxMarketConfigsView
import exchange.dydx.trading.feature.market.marketinfo.components.header.DydxMarketInfoHeaderView
import exchange.dydx.trading.feature.market.marketinfo.components.paging.DydxMarketInfoPagingView
import exchange.dydx.trading.feature.market.marketinfo.components.position.DydxMarketPositionView
import exchange.dydx.trading.feature.market.marketinfo.components.resources.DydxMarketResourcesView
import exchange.dydx.trading.feature.market.marketinfo.components.stats.DydxMarketStatsView
import exchange.dydx.trading.feature.market.marketinfo.components.tabs.DydxMarketAccountTabView
import exchange.dydx.trading.feature.market.marketinfo.components.tabs.DydxMarketStatsTabView
import exchange.dydx.trading.feature.market.marketinfo.components.tiles.DydxMarketTilesView
import exchange.dydx.trading.feature.portfolio.components.fills.DydxPortfolioFillsView.fillsListContent
import exchange.dydx.trading.feature.portfolio.components.fills.DydxPortfolioFillsViewModel
import exchange.dydx.trading.feature.portfolio.components.orders.DydxPortfolioOrdersView.ordersListContent
import exchange.dydx.trading.feature.portfolio.components.orders.DydxPortfolioOrdersViewModel
import exchange.dydx.trading.feature.trade.tradeinput.DydxTradeInputView
import kotlinx.coroutines.launch

@Preview
@Composable
fun Preview_DydxMarketInfoView() {
    DydxThemedPreviewSurface {
        DydxMarketInfoView.Content(Modifier, DydxMarketInfoView.ViewState.preview)
    }
}

object DydxMarketInfoView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val marketId: String? = null,
        val statsTabSelection: DydxMarketStatsTabView.Selection = DydxMarketStatsTabView.Selection.Statistics,
        val accountTabSelection: DydxMarketAccountTabView.Selection = DydxMarketAccountTabView.Selection.Position,
        val tileSelection: DydxMarketTilesView.TileType = DydxMarketTilesView.TileType.PRICE,
        val scrollToIndex: Int? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketInfoViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        val listState = PlatformRememberLazyListState(key = "DydxMarketInfoView")
        val scope = rememberCoroutineScope()

        val ordersViewModel: DydxPortfolioOrdersViewModel = hiltViewModel()
        val ordersViewState =
            ordersViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        val fillsViewModel: DydxPortfolioFillsViewModel = hiltViewModel()
        val fillsViewState =
            fillsViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        Box(
            modifier = modifier
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            Column {
                DydxMarketInfoHeaderView.Content(Modifier)

                PlatformDivider()

                LazyColumn(
                    modifier = Modifier,
                    state = listState,
                ) {
                    item(key = "pages") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .height(286.dp),
                        ) {
                            DydxMarketInfoPagingView.Content(
                                modifier = Modifier,
                            )
                        }
                        Spacer(modifier = Modifier.height(ThemeShapes.VerticalPadding))
                        DydxMarketTilesView.Content(
                            modifier = Modifier
                                .fillParentMaxWidth(),
                        )
                    }

                    stickyHeader(key = "account_tabs") {
                        DydxMarketAccountTabView.Content(
                            modifier = Modifier
                                .themeColor(ThemeColor.SemanticColor.layer_2)
                                .fillParentMaxWidth()
                                .padding(vertical = ThemeShapes.VerticalPadding * 2),
                        )
                    }

                    when (state.accountTabSelection) {
                        DydxMarketAccountTabView.Selection.Position -> {
                            item(key = "account") {
                                DydxMarketPositionView.Content(Modifier)
                            }
                        }

                        DydxMarketAccountTabView.Selection.Orders -> {
                            ordersListContent(ordersViewState)
                        }

                        DydxMarketAccountTabView.Selection.Trades -> {
                            fillsListContent(fillsViewState)
                        }

                        else -> {}
                    }

                    stickyHeader(key = "stats_tabs") {
                        DydxMarketStatsTabView.Content(
                            modifier = Modifier
                                .themeColor(ThemeColor.SemanticColor.layer_2)
                                .fillParentMaxWidth()
                                .padding(vertical = ThemeShapes.VerticalPadding * 2),
                        )
                    }
                    item(key = "stats") {
                        AnimatedVisibility(
                            visible =
                            state.statsTabSelection == DydxMarketStatsTabView.Selection.Statistics,
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            DydxMarketStatsView.Content(Modifier)
                        }
                        AnimatedVisibility(
                            visible =
                            state.statsTabSelection == DydxMarketStatsTabView.Selection.About,
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            Column(
                                modifier = Modifier,
                            ) {
                                DydxMarketResourcesView.Content(Modifier)
                                Spacer(modifier = Modifier.height(ThemeShapes.VerticalPadding))
                                DydxMarketConfigsView.Content(Modifier)
                            }
                        }
                    }
                    item(key = "spacer") {
                        Row(
                            modifier = Modifier
                                .height(DydxTradeInputView.sheetPeekHeight),
                        ) {}
                    }

                    if (state.scrollToIndex != null) {
                        scope.launch {
                            listState.animateScrollToItem(state.scrollToIndex)
                        }
                    }
                }
            }
        }
    }
}
