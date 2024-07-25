package exchange.dydx.trading.feature.trade.trigger.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.account.SubaccountPosition
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class DydxTriggerOrderReceiptViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    private val marketIdFlow = abacusStateManager.state.triggerOrdersInput
        .mapNotNull { it?.marketId }

    val state: Flow<DydxTriggerOrderReceiptView.ViewState?> =
        combine(
            marketIdFlow
                .flatMapLatest {
                    abacusStateManager.state.market(marketId = it)
                }
                .filterNotNull()
                .distinctUntilChanged(),
            abacusStateManager.state.configsAndAssetMap,
            marketIdFlow
                .flatMapLatest { marketId ->
                    abacusStateManager.state.selectedSubaccountPositionOfMarket(marketId)
                }
                .filterNotNull()
                .distinctUntilChanged(),
        ) { market, configsAndAssetMap, position ->
            createViewState(market, configsAndAssetMap?.get(market.id), position)
        }
            .distinctUntilChanged()

    private fun createViewState(
        market: PerpetualMarket,
        configsAndAsset: MarketConfigsAndAsset?,
        position: SubaccountPosition,
    ): DydxTriggerOrderReceiptView.ViewState {
        return DydxTriggerOrderReceiptView.ViewState(
            localizer = localizer,
            entryPrice = formatter.dollar(
                position.entryPrice?.current,
                configsAndAsset?.configs?.tickSizeDecimals ?: 0,
            ),
            oraclePrice = formatter.dollar(
                market.oraclePrice,
                configsAndAsset?.configs?.tickSizeDecimals ?: 0,
            ),
        )
    }
}
