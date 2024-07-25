package exchange.dydx.trading.feature.market.marketinfo.components.position

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.SubaccountPendingPosition
import exchange.dydx.abacus.output.account.SubaccountPosition
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.PortfolioRoutes
import exchange.dydx.trading.common.navigation.TradeRoutes
import exchange.dydx.trading.feature.market.marketinfo.components.tabs.DydxMarketAccountTabView
import exchange.dydx.trading.feature.market.marketinfo.streams.MarketAndAsset
import exchange.dydx.trading.feature.market.marketinfo.streams.MarketInfoStreaming
import exchange.dydx.trading.feature.portfolio.components.pendingpositions.DydxPortfolioPendingPositionItemView
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketPositionViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@HiltViewModel
class DydxMarketPositionViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val formatter: DydxFormatter,
    marketInfoStream: MarketInfoStreaming,
    private val router: DydxRouter,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val accountTabFlow: MutableStateFlow<@JvmSuppressWildcards DydxMarketAccountTabView.Selection>,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketPositionView.ViewState?> =
        combine(
            marketInfoStream.selectedSubaccountPosition,
            marketInfoStream.marketAndAsset.filterNotNull(),
            abacusStateManager.state.selectedSubaccountPendingPositions,
        ) { selectedSubaccountPosition, marketAndAsset, selectedSubaccountPendingPositions ->
            val pendingPosition = selectedSubaccountPendingPositions?.firstOrNull { it.marketId == marketAndAsset.market.id }
            createViewState(selectedSubaccountPosition, marketAndAsset, pendingPosition)
        }
            .distinctUntilChanged()

    private fun createViewState(
        position: SubaccountPosition?,
        marketAndAsset: MarketAndAsset,
        pendingPosition: SubaccountPendingPosition?
    ): DydxMarketPositionView.ViewState {
        return DydxMarketPositionView.ViewState(
            localizer = localizer,
            shareAction = {},
            closeAction = {
                router.navigateTo(
                    route = TradeRoutes.close_position + "/${marketAndAsset.market.id}",
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
            marginEditAction = if (position?.marginMode == MarginMode.Isolated) {
                {
                    router.navigateTo(
                        route = TradeRoutes.adjust_margin + "/${marketAndAsset.market.id}",
                        presentation = DydxRouter.Presentation.Modal,
                    )
                }
            } else {
                null
            },
            sharedMarketPositionViewState = position?.let {
                SharedMarketPositionViewState.create(
                    position = position,
                    market = marketAndAsset.market,
                    asset = marketAndAsset.asset,
                    formatter = formatter,
                    localizer = localizer,
                    onAdjustMarginAction = {
                        router.navigateTo(
                            route = TradeRoutes.adjust_margin + "/${marketAndAsset.market.id}",
                            presentation = DydxRouter.Presentation.Modal,
                        )
                    },
                )
            },
            pendingPosition = pendingPosition?.let { position ->
                DydxPortfolioPendingPositionItemView.ViewState(
                    localizer = localizer,
                    id = position.marketId,
                    logoUrl = marketAndAsset.asset.resources?.imageUrl,
                    marketName = marketAndAsset.asset.name,
                    margin = formatter.dollar(position.freeCollateral?.current, 2),
                    viewOrderAction = {
                        accountTabFlow.value = DydxMarketAccountTabView.Selection.Orders
                    },
                    cancelOrderAction = {
                        router.navigateTo(
                            route = PortfolioRoutes.cancel_pending_position + "/${position.marketId}",
                            presentation = DydxRouter.Presentation.Modal,
                        )
                    },
                )
            },
        )
    }
}
