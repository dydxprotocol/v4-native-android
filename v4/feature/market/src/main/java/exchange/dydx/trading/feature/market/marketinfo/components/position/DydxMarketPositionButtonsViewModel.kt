package exchange.dydx.trading.feature.market.marketinfo.components.position

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.TradeRoutes
import exchange.dydx.trading.feature.market.marketinfo.streams.MarketAndAsset
import exchange.dydx.trading.feature.market.marketinfo.streams.MarketInfoStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxMarketPositionButtonsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    marketInfoStream: MarketInfoStreaming,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketPositionButtonsView.ViewState?> =
        marketInfoStream.marketAndAsset.filterNotNull()
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        marketAndAsset: MarketAndAsset,
    ): DydxMarketPositionButtonsView.ViewState {
        return DydxMarketPositionButtonsView.ViewState(
            localizer = localizer,
            addTriggerAction = {
                router.navigateTo(
                    route = TradeRoutes.trigger + "/${marketAndAsset.market.id}",
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
            closeAction = {
                router.navigateTo(
                    route = TradeRoutes.close_position + "/${marketAndAsset.market.id}",
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
        )
    }
}
