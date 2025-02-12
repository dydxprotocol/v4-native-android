package exchange.dydx.trading.feature.market.marketlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import exchange.dydx.trading.feature.market.marketlist.components.DydxMarketAssetFilterView
import exchange.dydx.trading.feature.market.marketlist.components.DydxMarketAssetItemView
import exchange.dydx.trading.feature.market.marketlist.components.DydxMarketAssetSortView
import exchange.dydx.trading.feature.market.marketlist.components.DydxMarketHeaderView
import exchange.dydx.trading.feature.market.marketlist.components.DydxMarketSummaryView
import exchange.dydx.trading.feature.shared.bottombar.DydxBottomBarScaffold
import kotlinx.coroutines.launch

@Preview
@Composable
fun Preview_DydxMarketAssetListView() {
    DydxThemedPreviewSurface {
        DydxMarketAssetListView.Content(Modifier, DydxMarketAssetListView.ViewState.preview)
    }
}

object DydxMarketAssetListView : DydxComponent {

    private var firstTime = true

    data class ViewState(
        val localizer: LocalizerProtocol,
        val items: List<DydxMarketAssetItemView.ViewState>,
        val scrollToTop: Boolean = false,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                items = listOf(
                    DydxMarketAssetItemView.ViewState.preview,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketAssetListViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        DydxBottomBarScaffold(Modifier) {
            Content(it, state)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        val listState = PlatformRememberLazyListState(key = "DydxMarketAssetListView")
        val scope = rememberCoroutineScope()

        Box(
            modifier = modifier
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            Column {
                DydxMarketHeaderView.Content(
                    Modifier
                        .height(72.dp)
                        .padding(horizontal = ThemeShapes.HorizontalPadding)
                        .padding(vertical = ThemeShapes.VerticalPadding),
                )

                LazyColumn(
                    modifier = Modifier,
                    state = listState,
                ) {
                    item(key = "summary") {
//                        DydxPredictionMarketBannerView.Content(
//                            Modifier.padding(
//                                horizontal = ThemeShapes.HorizontalPadding,
//                            )
//                                .padding(bottom = ThemeShapes.VerticalPadding * 2),
//                        )
                        DydxMarketSummaryView.Content(Modifier.padding(horizontal = ThemeShapes.HorizontalPadding))
                    }
                    stickyHeader(key = "filter") {
                        Column(
                            Modifier
                                .fillParentMaxWidth()
                                .themeColor(ThemeColor.SemanticColor.layer_2),
                        ) {
                            DydxMarketAssetFilterView.Content(
                                Modifier.padding(
                                    vertical = 8.dp,
                                ),
                            )
                            DydxMarketAssetSortView.Content(
                                Modifier
                                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                                    .padding(bottom = ThemeShapes.VerticalPadding),
                            )
                        }
                    }
                    items(items = state.items, key = { it.id }) { item ->
                        Column(
                            Modifier
                                .animateItemPlacement(),
                        ) {
                            if (item == state.items.first()) {
                                PlatformDivider()
                            }
                            DydxMarketAssetItemView.Content(
                                modifier = Modifier,
                                state = item,
                            )
                            if (item != state.items.last()) {
                                PlatformDivider()
                            }
                        }
                    }

                    item(key = "bottom") {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .padding(bottom = 69.dp),
                        )
                    }

                    if (state.scrollToTop || firstTime) {
                        if (firstTime) {
                            firstTime = false
                        }
                        scope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }
                }
            }
        }
    }
}
