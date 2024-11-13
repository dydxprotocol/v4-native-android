package exchange.dydx.trading.feature.portfolio.components.pendingpositions

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.SubaccountPendingPosition
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.MarketRoutes
import exchange.dydx.trading.common.navigation.PortfolioRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxPortfolioPendingPositionsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxPortfolioPendingPositionsView.ViewState?> =
        combine(
            abacusStateManager.state.selectedSubaccountPendingPositions,
            abacusStateManager.state.configsAndAssetMap,
        ) { positions, configsAndAssetMap ->
            createViewState(
                position = positions,
                configsAndAssetMap = configsAndAssetMap,
            )
        }
            .distinctUntilChanged()

    private fun createViewState(
        position: List<SubaccountPendingPosition>?,
        configsAndAssetMap: Map<String, MarketConfigsAndAsset>?,
    ): DydxPortfolioPendingPositionsView.ViewState {
        return DydxPortfolioPendingPositionsView.ViewState(
            localizer = localizer,
            positions = position?.mapNotNull { position ->
                val configsAndAsset = configsAndAssetMap?.get(position.marketId) ?: return@mapNotNull null
                DydxPortfolioPendingPositionItemView.ViewState(
                    localizer = localizer,
                    id = position.marketId,
                    logoUrl = configsAndAsset.asset?.resources?.imageUrl,
                    marketName = configsAndAsset.asset?.name,
                    margin = formatter.dollar(position.freeCollateral?.current, 2),
                    viewOrderAction = {
                        router.navigateTo(
                            route = MarketRoutes.marketInfo + "/${position.marketId}?currentSection=Orders",
                        )
                    },
                    cancelOrderAction = {
                        router.navigateTo(
                            route = PortfolioRoutes.cancel_pending_position + "/${position.marketId}",
                            presentation = DydxRouter.Presentation.Modal,
                        )
                    },
                )
            }?.distinctBy {
                it.id
            } ?: listOf(),
        )
    }
}
