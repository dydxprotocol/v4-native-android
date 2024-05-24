package exchange.dydx.trading.feature.portfolio.components.orders

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.DydxFeatureFlag
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.PortfolioRoutes
import exchange.dydx.trading.feature.shared.viewstate.SharedOrderViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxPortfolioOrdersViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    private var featureFlags: DydxFeatureFlags,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxPortfolioOrdersView.ViewState?> = combine(
        abacusStateManager.marketId,
        abacusStateManager.state.selectedSubaccountOrders,
        abacusStateManager.state.marketMap,
        abacusStateManager.state.assetMap,
    ) { marketId, orders, marketMap, assetMap ->
        createViewState(marketId, orders, marketMap, assetMap)
    }
        .distinctUntilChanged()

    private fun createViewState(
        marketId: String?,
        orders: List<SubaccountOrder>?,
        marketMap: Map<String, PerpetualMarket>?,
        assetMap: Map<String, Asset>?,
    ): DydxPortfolioOrdersView.ViewState {
        val filteredOrders = if (marketId != null) {
            orders?.filter { it.marketId == marketId }
        } else {
            orders
        }
        return DydxPortfolioOrdersView.ViewState(
            localizer = localizer,
            orders = filteredOrders?.mapNotNull { order ->
                SharedOrderViewState.create(
                    localizer = localizer,
                    formatter = formatter,
                    order = order,
                    marketMap = marketMap,
                    assetMap = assetMap,
                )
            } ?: listOf(),
            onOrderTappedAction = { orderId ->
                router.navigateTo(
                    route = PortfolioRoutes.order_details + "/$orderId",
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
            onBackTappedAction = {
                router.navigateBack()
            },
            isIsolatedMarketEnabled = featureFlags.isFeatureEnabled(DydxFeatureFlag.enable_isolated_market),
        )
    }
}
