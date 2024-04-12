package exchange.dydx.trading.feature.market.marketinfo.components.position

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.SubaccountPosition
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.dydxstatemanager.stopLossOrders
import exchange.dydx.dydxstatemanager.takeProfitOrders
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.TradeRoutes
import exchange.dydx.trading.feature.market.marketinfo.streams.MarketInfoStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class DydxMarketPositionButtonsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    marketInfoStream: MarketInfoStreaming,
    private val router: DydxRouter,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    private val marketIdFlow = marketInfoStream.marketAndAsset
        .mapNotNull { it?.market?.id }

    val state: Flow<DydxMarketPositionButtonsView.ViewState?> =
        combine(
            marketIdFlow,
            marketIdFlow.flatMapLatest { abacusStateManager.state.selectedSubaccountPositionOfMarket(it) },
            marketIdFlow.flatMapLatest { abacusStateManager.state.takeProfitOrders(it) },
            marketIdFlow.flatMapLatest { abacusStateManager.state.stopLossOrders(it) },
            abacusStateManager.state.configsAndAssetMap,
        ) { marketId, position, takeProfitOrders, stopLossOrders, configsAndAssetMap ->
            createViewState(marketId, position, takeProfitOrders, stopLossOrders, configsAndAssetMap?.get(marketId))
        }
            .distinctUntilChanged()

    private fun createViewState(
        marketId: String,
        position: SubaccountPosition?,
        takeProfitOrders: List<SubaccountOrder>?,
        stopLossOrders: List<SubaccountOrder>?,
        configsAndAsset: MarketConfigsAndAsset?,
    ): DydxMarketPositionButtonsView.ViewState {
        return DydxMarketPositionButtonsView.ViewState(
            localizer = localizer,
            addTriggerAction = {
                router.navigateTo(
                    route = TradeRoutes.trigger + "/$marketId",
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
            closeAction = {
                router.navigateTo(
                    route = TradeRoutes.close_position + "/$marketId",
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
            takeProfitTrigger = createTriggerViewState(
                label = "TP",
                position = position,
                orders = takeProfitOrders,
                configsAndAsset = configsAndAsset,
            ),
            stopLossTrigger = createTriggerViewState(
                label = "SL",
                position = position,
                orders = stopLossOrders,
                configsAndAsset = configsAndAsset,
            ),
        )
    }

    private fun createTriggerViewState(
        label: String,
        position: SubaccountPosition?,
        orders: List<SubaccountOrder>?,
        configsAndAsset: MarketConfigsAndAsset?,
    ): DydxMarketPositionButtonsView.TriggerViewState? {
        val order = orders?.firstOrNull()
            ?: return null

        val tickSize = configsAndAsset?.configs?.displayTickSizeDecimals ?: 0
        val size = order.size
        val positionSize = position?.size?.current ?: 0.0
        val percentage = if (positionSize > 0.0) {
            size / positionSize
        } else {
            0.0
        }
        return DydxMarketPositionButtonsView.TriggerViewState(
            label = label,
            triggerPrice = order.triggerPrice?.let { formatter.dollar(it, tickSize) },
            limitPrice = order.price.let { formatter.dollar(it, tickSize) },
            sizePercent = formatter.percent(percentage, 2),
            hasMultipleOrders = orders.size > 1,
        )
    }
}
