package exchange.dydx.trading.feature.market.marketinfo.streams

import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.output.account.SubaccountPosition
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.favorite.DydxFavoriteStoreProtocol
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

interface MarketInfoStreaming {
    val market: Flow<PerpetualMarket?>
    val marketAndAsset: Flow<MarketAndAsset?>
    val sharedMarketViewState: Flow<SharedMarketViewState?>
    val selectedSubaccountPosition: Flow<SubaccountPosition?>
}

interface MutableMarketInfoStreaming : MarketInfoStreaming {
    fun update(marketId: String?)
}

@ActivityRetainedScoped
class MarketInfoStream @Inject constructor(
    val abacusStateManager: AbacusStateManagerProtocol,
    val formatter: DydxFormatter,
    val localizer: LocalizerProtocol,
    val favoriteStore: DydxFavoriteStoreProtocol,
    @CoroutineScopes.App private val streamScope: CoroutineScope,
) : MutableMarketInfoStreaming {
    override fun update(marketId: String?) {
        abacusStateManager.setMarket(marketId)
        abacusStateManager.startTrade()
    }

    override val market: Flow<PerpetualMarket?> =
        abacusStateManager.marketId
            .filterNotNull()
            .flatMapLatest { marketId ->
                abacusStateManager.state.market(marketId)
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(streamScope, SharingStarted.Lazily, 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val marketAndAsset: Flow<MarketAndAsset?> =
        abacusStateManager.marketId
            .filterNotNull()
            .flatMapLatest { marketId ->
                combine(
                    abacusStateManager.state.market(marketId),
                    abacusStateManager.state.assetMap,
                ) { market, assetMap ->
                    if (market != null && assetMap != null) {
                        val asset = assetMap[market.assetId]
                        if (asset != null) {
                            MarketAndAsset(market, asset)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(streamScope, SharingStarted.Lazily, 1)

    override val selectedSubaccountPosition: Flow<SubaccountPosition?> =
        combine(
            abacusStateManager.marketId,
            abacusStateManager.state.selectedSubaccountPositions,
        ) { marketId, selectedSubaccountPositions ->
            if (marketId != null && selectedSubaccountPositions != null) {
                selectedSubaccountPositions.firstOrNull { position ->
                    position.id == marketId && (position.side.current == PositionSide.SHORT || position.side.current == PositionSide.LONG)
                }
            } else {
                null
            }
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(streamScope, SharingStarted.Lazily, 1)

    override val sharedMarketViewState: Flow<SharedMarketViewState?> =
        combine(
            marketAndAsset,
            favoriteStore.state,
        ) { marketAndAsset, _ ->
            if (marketAndAsset != null) {
                SharedMarketViewState.create(
                    market = marketAndAsset.market,
                    asset = marketAndAsset.asset,
                    formatter = formatter,
                    localizer = localizer,
                    favoriteStore = favoriteStore,
                )
            } else {
                null
            }
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(streamScope, SharingStarted.Lazily, 1)
}

data class MarketAndAsset(
    val market: PerpetualMarket,
    val asset: Asset,
)
