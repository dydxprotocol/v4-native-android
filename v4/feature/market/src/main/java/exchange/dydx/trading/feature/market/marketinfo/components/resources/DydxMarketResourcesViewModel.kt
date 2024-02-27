package exchange.dydx.trading.feature.market.marketinfo.components.resources

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.market.marketinfo.streams.MarketInfoStreaming
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxMarketResourcesViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
    marketInfoStream: MarketInfoStreaming,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketResourcesView.ViewState?> =
        marketInfoStream.sharedMarketViewState
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(sharedMarketViewState: SharedMarketViewState?): DydxMarketResourcesView.ViewState {
        return DydxMarketResourcesView.ViewState(
            localizer = localizer,
            sharedMarketViewState = sharedMarketViewState,
            urlHandler = { url ->
                router.navigateTo(url)
            },
        )
    }
}
