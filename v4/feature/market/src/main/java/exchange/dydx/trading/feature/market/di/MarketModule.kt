package exchange.dydx.trading.feature.market.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.feature.market.marketinfo.components.tabs.DydxMarketAccountTabView
import exchange.dydx.trading.feature.market.marketinfo.components.tabs.DydxMarketStatsTabView
import exchange.dydx.trading.feature.market.marketinfo.components.tiles.DydxMarketTilesView
import exchange.dydx.trading.feature.market.marketinfo.streams.MarketInfoStream
import exchange.dydx.trading.feature.market.marketinfo.streams.MarketInfoStreaming
import exchange.dydx.trading.feature.market.marketinfo.streams.MutableMarketInfoStreaming
import exchange.dydx.trading.feature.market.marketlist.components.FilterAction
import exchange.dydx.trading.feature.market.marketlist.components.SortAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Module
@InstallIn(ActivityRetainedComponent::class)
interface MarketListModule {

    @Binds fun bindFilterActionFlow(
        mutableFlow: MutableStateFlow<FilterAction>,
    ): Flow<FilterAction>

    @Binds fun bindSortActionFlow(
        mutableFlow: MutableStateFlow<SortAction>,
    ): Flow<SortAction>

    companion object {
        @Provides
        @ActivityRetainedScoped
        fun provideMutableFilterActionFlow(localizer: LocalizerProtocol): MutableStateFlow<FilterAction> {
            return MutableStateFlow(FilterAction.actions(localizer).first())
        }

        @Provides
        @ActivityRetainedScoped
        fun provideMutableSortActionFlow(localizer: LocalizerProtocol): MutableStateFlow<SortAction> {
            return MutableStateFlow(SortAction.actions(localizer).first())
        }
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
interface MarketInfoModule {

    @Binds
    fun bindMutableMarketInfoStreaming(
        marketInfoStream: MarketInfoStream,
    ): MutableMarketInfoStreaming

    @Binds
    fun bindMarketInfoStreaming(
        mutableMarketInfoStream: MutableMarketInfoStreaming,
    ): MarketInfoStreaming

    @Binds
    fun bindStatsTabSelection(
        mutableStatsTabFlow: MutableStateFlow<DydxMarketStatsTabView.Selection>,
    ): Flow<DydxMarketStatsTabView.Selection>

    @Binds
    fun bindAccountTabSelection(
        mutableAccountTabFlow: MutableStateFlow<DydxMarketAccountTabView.Selection>,
    ): Flow<DydxMarketAccountTabView.Selection>

    @Binds
    fun bindTileSelection(
        mutableTileFlow: MutableStateFlow<DydxMarketTilesView.Tile>,
    ): Flow<DydxMarketTilesView.Tile>

    companion object {
        @Provides
        @ActivityRetainedScoped
        fun provideMutableStatsTabSelection(): MutableStateFlow<DydxMarketStatsTabView.Selection> {
            return MutableStateFlow(DydxMarketStatsTabView.Selection.Statistics)
        }

        @Provides
        @ActivityRetainedScoped
        fun provideMutableAccountTabSelection(): MutableStateFlow<DydxMarketAccountTabView.Selection> {
            return MutableStateFlow(DydxMarketAccountTabView.Selection.Position)
        }

        @Provides
        @ActivityRetainedScoped
        fun provideMutableTileSelection(): MutableStateFlow<DydxMarketTilesView.Tile> {
            return MutableStateFlow(DydxMarketTilesView.Tile(DydxMarketTilesView.TileType.PRICE))
        }
    }
}
