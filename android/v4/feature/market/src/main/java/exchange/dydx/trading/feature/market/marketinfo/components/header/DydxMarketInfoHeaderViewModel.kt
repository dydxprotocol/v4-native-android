package exchange.dydx.trading.feature.market.marketinfo.components.header

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.clientState.favorite.DydxFavoriteStoreProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.market.marketinfo.streams.MarketInfoStreaming
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxMarketInfoHeaderViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
    marketInfoStream: MarketInfoStreaming,
    private val favoriteStore: DydxFavoriteStoreProtocol
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketInfoHeaderView.ViewState?> =
        marketInfoStream.sharedMarketViewState
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        sharedMarketViewState: SharedMarketViewState?,
    ): DydxMarketInfoHeaderView.ViewState {
        return DydxMarketInfoHeaderView.ViewState(
            localizer = localizer,
            sharedMarketViewState = sharedMarketViewState,
            backAction = {
                router.navigateBack()
            },
            toggleFavoriteAction = {
                val marketId = sharedMarketViewState?.id ?: return@ViewState
                val isFavorite = favoriteStore.isFavorite(marketId)
                favoriteStore.setFavorite(!isFavorite, marketId)
            },
        )
    }
}
