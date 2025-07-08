package exchange.dydx.trading.feature.portfolio.components.positions

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.account.SubaccountPosition
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.MarketRoutes
import exchange.dydx.trading.common.navigation.TradeRoutes
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketPositionViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxPortfolioPositionsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {
    val state: Flow<DydxPortfolioPositionsView.ViewState?> =
        combine(
            abacusStateManager.state.selectedSubaccountPositions,
            abacusStateManager.state.marketMap,
            abacusStateManager.state.assetMap,
            abacusStateManager.state.onboarded,
        ) { positions, marketMap, assetMap, onboarded ->
            createViewState(
                position = positions,
                marketMap = marketMap,
                assetMap = assetMap,
                onboarded = onboarded,
            )
        }
            .distinctUntilChanged()

    private fun createViewState(
        position: List<SubaccountPosition>?,
        marketMap: Map<String, PerpetualMarket>?,
        assetMap: Map<String, Asset>?,
        onboarded: Boolean,
    ): DydxPortfolioPositionsView.ViewState {
        return DydxPortfolioPositionsView.ViewState(
            localizer = localizer,
            positions = position?.mapNotNull { position ->
                val market = marketMap?.get(position.id) ?: return@mapNotNull null
                SharedMarketPositionViewState.create(
                    position = position,
                    market = market,
                    asset = assetMap?.get(position.assetId),
                    formatter = formatter,
                    localizer = localizer,
                    onAdjustMarginAction = {
                        router.navigateTo(
                            route = TradeRoutes.adjust_margin + "/${market.id}",
                            presentation = DydxRouter.Presentation.Modal,
                        )
                    },
                )
            }?.distinctBy {
                it.id
            } ?: listOf(),
            onPositionTapAction = { position ->
                val market = marketMap?.get(position.id) ?: return@ViewState
                router.navigateTo(
                    route = MarketRoutes.marketInfo + "/${market.id}",
                    presentation = DydxRouter.Presentation.Push,
                )
            },
            onboarded = onboarded,
        )
    }
}
