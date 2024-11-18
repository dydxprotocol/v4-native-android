package exchange.dydx.trading.feature.market.marketlist

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.favorite.DydxFavoriteStoreProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.MarketRoutes
import exchange.dydx.trading.feature.market.marketlist.components.DydxMarketAssetItemView
import exchange.dydx.trading.feature.market.marketlist.components.FilterAction
import exchange.dydx.trading.feature.market.marketlist.components.SortAction
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxMarketAssetListViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val filterActionFlow: Flow<FilterAction>,
    private val sortActionFlow: Flow<SortAction>,
    private val router: DydxRouter,
    private val favoriteStore: DydxFavoriteStoreProtocol,
) : ViewModel(), DydxViewModel {

    private var currentFilterAction: FilterAction? = null
    private var currentSortAction: SortAction? = null

    val state: Flow<DydxMarketAssetListView.ViewState?> =
        combine(
            abacusStateManager.state.marketList,
            abacusStateManager.state.assetMap,
            filterActionFlow,
            sortActionFlow,
            favoriteStore.state,
        ) { markets, assetMap, filterAction, sortAction, _ ->
            val filteredMarkets = markets?.filter { market ->
                if (market.status?.canTrade != true) {
                    return@filter false
                }
                val action = filterAction.action
                if (assetMap != null) {
                    return@filter action(market, assetMap, favoriteStore)
                } else {
                    return@filter true
                }
            }
            val sortedMarkets = filteredMarkets?.sortedWith { market1, market2 ->
                val action = sortAction.action
                return@sortedWith action(market1, market2)
            }
            val scrollTop = currentFilterAction != null && currentSortAction != null &&
                (currentFilterAction != filterAction || currentSortAction != sortAction)
            currentFilterAction = filterAction
            currentSortAction = sortAction
            createViewState(sortedMarkets, assetMap, scrollTop)
        }
            .distinctUntilChanged()

    private fun createViewState(
        markets: List<PerpetualMarket>?,
        assetMap: Map<String, Asset>?,
        scrollTop: Boolean,
    ): DydxMarketAssetListView.ViewState {
        return DydxMarketAssetListView.ViewState(
            localizer = localizer,
            items = markets?.map { market ->
                val asset = assetMap?.get(market.assetId)
                DydxMarketAssetItemView.ViewState(
                    localizer = localizer,
                    sharedMarketViewState = SharedMarketViewState.create(
                        market,
                        asset,
                        formatter,
                        localizer,
                        favoriteStore,
                    ),
                    onTapAction = {
                        router.navigateTo(
                            route = MarketRoutes.marketInfo + "/${market.id}",
                            presentation = DydxRouter.Presentation.Push,
                        )
                    },
                    toggleFavoriteAction = {
                        val isFavorite = favoriteStore.isFavorite(market.id)
                        favoriteStore.setFavorite(!isFavorite, market.id)
                    },
                )
            }?.distinctBy {
                it.sharedMarketViewState?.id
            } ?: emptyList(),
            scrollToTop = scrollTop,
        )
    }
}
