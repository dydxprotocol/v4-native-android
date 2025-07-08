package exchange.dydx.trading.feature.trade.closeposition.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.output.account.SubaccountPosition
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.favorite.DydxFavoriteStoreProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class DydxClosePositionHeaderViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    private val favoriteStore: DydxFavoriteStoreProtocol,
) : ViewModel(), DydxViewModel {

    private val market: Flow<PerpetualMarket?> =
        abacusStateManager.state.closePositionInput.mapNotNull { it?.marketId }
            .flatMapLatest { marketId ->
                abacusStateManager.state.market(marketId)
            }
            .distinctUntilChanged()

    val state: Flow<DydxClosePositionHeaderView.ViewState?> =
        combine(
            market.filterNotNull(),
            abacusStateManager.state.selectedSubaccountPositions,
            abacusStateManager.state.assetMap.filterNotNull(),
        ) { market, selectedSubaccountPositions, assetMap ->
            val position = selectedSubaccountPositions?.firstOrNull { it.id == market.id } ?: return@combine null
            createViewState(market, position, assetMap)
        }
            .distinctUntilChanged()

    private fun createViewState(
        market: PerpetualMarket,
        position: SubaccountPosition,
        assetMap: Map<String, Asset>,
    ): DydxClosePositionHeaderView.ViewState {
        val asset = assetMap[market.assetId]
        return DydxClosePositionHeaderView.ViewState(
            localizer = localizer,
            sharedMarketViewState = SharedMarketViewState.create(market, asset, formatter, localizer, favoriteStore),
            side = SideTextView.ViewState(
                localizer = localizer,
                side = when (position.side.current) {
                    PositionSide.LONG -> SideTextView.Side.Long
                    PositionSide.SHORT -> SideTextView.Side.Short
                    else -> SideTextView.Side.None
                },
                coloringOption = SideTextView.ColoringOption.COLORED,
            ),
            closeAction = {
                router.navigateBack()
            },
        )
    }
}
