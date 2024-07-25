package exchange.dydx.trading.feature.portfolio.components.fills

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.PortfolioRoutes
import exchange.dydx.trading.feature.shared.viewstate.SharedFillViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxPortfolioFillsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxPortfolioFillsView.ViewState?> = combine(
        abacusStateManager.marketId,
        abacusStateManager.state.selectedSubaccountFills,
        abacusStateManager.state.marketMap,
        abacusStateManager.state.assetMap,
    ) { marketId, fills, marketMap, assetMap ->
        createViewState(marketId, fills, marketMap, assetMap)
    }
        .distinctUntilChanged()

    private fun createViewState(
        marketId: String?,
        fills: List<SubaccountFill>?,
        marketMap: Map<String, PerpetualMarket>?,
        assetMap: Map<String, Asset>?,
    ): DydxPortfolioFillsView.ViewState {
        val filteredFills = if (marketId != null) {
            fills?.filter { it.marketId == marketId }
        } else {
            fills
        }
        return DydxPortfolioFillsView.ViewState(
            localizer = localizer,
            fills = filteredFills?.mapNotNull { fill ->
                SharedFillViewState.create(
                    localizer = localizer,
                    formatter = formatter,
                    fill = fill,
                    marketMap = marketMap,
                    assetMap = assetMap,
                )
            } ?: listOf(),
            onFillTappedAction = { fillId ->
                router.navigateTo(
                    route = PortfolioRoutes.order_details + "/$fillId",
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
        )
    }
}
