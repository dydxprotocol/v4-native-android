package exchange.dydx.trading.feature.portfolio.components.pendingpositions

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.output.SubaccountPendingPosition
import exchange.dydx.abacus.output.SubaccountPosition
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.DydxFeatureFlag
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxPortfolioPendingPositionsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
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
                DydxPortfolioPendingPositionView.ViewState(
                    localizer = localizer,
                    logoUrl = configsAndAsset.asset?.resources?.imageUrl,
                    marketName = configsAndAsset.asset?.name,
                    margin = formatter.dollar(position.freeCollateral?.current, 2),
                    viewOrderAction = {},
                    cancelOrderAction = {},
                )
            } ?: listOf()
        )
    }
}
