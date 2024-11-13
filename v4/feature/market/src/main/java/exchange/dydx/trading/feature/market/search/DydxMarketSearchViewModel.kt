package exchange.dydx.trading.feature.market.search

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
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxMarketSearchViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    private val favoriteStore: DydxFavoriteStoreProtocol,
) : ViewModel(), DydxViewModel {

    private val searchText: MutableStateFlow<String> = MutableStateFlow("")

    val state: Flow<DydxMarketSearchView.ViewState?> =
        combine(
            abacusStateManager.state.marketList,
            abacusStateManager.state.assetMap,
            searchText,
            favoriteStore.state,
        ) { markets, assetMap, searchText, _ ->
            val filterMarkets = markets?.filter { market ->
                if (market.status?.canTrade != true) {
                    return@filter false
                }
                if (searchText.isEmpty()) {
                    return@filter false
                }
                val asset = assetMap?.get(market.assetId) ?: return@filter false

                asset.displayableAssetId.lowercase().contains(searchText.lowercase()) ||
                    (asset.name?.lowercase()?.contains(searchText.lowercase()) ?: false)
            }
            createViewState(filterMarkets, assetMap)
        }
            .distinctUntilChanged()

    private fun createViewState(
        markets: List<PerpetualMarket>?,
        assetMap: Map<String, Asset>?,
    ): DydxMarketSearchView.ViewState {
        return DydxMarketSearchView.ViewState(
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
                        router.navigateBack()
                        router.navigateTo(
                            route = MarketRoutes.marketInfo + "/${market.id}",
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
            searchText = searchText.value,
            searchTextChanged = {
                searchText.value = it
            },
            closeAction = {
                router.navigateBack()
            },
        )
    }
}
