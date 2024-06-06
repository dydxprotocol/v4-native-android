package exchange.dydx.trading.feature.trade.margin.components.header

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class DydxAdjustMarginInputHeaderViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    private val market: Flow<PerpetualMarket?> =
        abacusStateManager.marketId.mapNotNull { it }
            .flatMapLatest { marketId ->
                abacusStateManager.state.market(marketId)
            }
            .distinctUntilChanged()

    val state: Flow<DydxAdjustMarginInputHeaderView.ViewState?> =
        combine(
            market,
            abacusStateManager.state.assetMap.filterNotNull(),
        ) { market, assetMap ->
            createViewState(market, assetMap)
        }
            .distinctUntilChanged()

    private fun createViewState(
        market: PerpetualMarket?,
        assetMap: Map<String, Asset>,
    ): DydxAdjustMarginInputHeaderView.ViewState {
        val logoUrl = market?.assetId?.let {
            assetMap[it]?.resources?.imageUrl
        }
        return DydxAdjustMarginInputHeaderView.ViewState(
            localizer = localizer,
            logoUrl = logoUrl,
            closeAction = {
                router.navigateBack()
            },
        )
    }
}
