package exchange.dydx.trading.feature.market.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.favorite.DydxFavoriteStoreProtocol
import exchange.dydx.trading.common.formatter.DydxFormatter
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

// @InstallIn(ViewModelComponent::class)
@Module
@InstallIn(ActivityRetainedComponent::class)
object MarketListModule {

    @Provides
    @ActivityRetainedScoped
    fun provideFilterActionFlow(
        mutableFlow: MutableStateFlow<FilterAction?>,
    ): Flow<FilterAction?> {
        return mutableFlow
    }

    @Provides
    @ActivityRetainedScoped
    fun provideMutableFilterActionFlow(): MutableStateFlow<FilterAction?> {
        return MutableStateFlow(null)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideSortActionFlow(
        mutableFlow: MutableStateFlow<SortAction?>,
    ): Flow<SortAction?> {
        return mutableFlow
    }

    @Provides
    @ActivityRetainedScoped
    fun provideMutableSortActionFlow(): MutableStateFlow<SortAction?> {
        return MutableStateFlow(null)
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object MarketIfoModule {
    @Provides
    @ActivityRetainedScoped
    fun provideMutableMarketInfoStream(
        abacusStateManager: AbacusStateManagerProtocol,
        formatter: DydxFormatter,
        localizer: LocalizerProtocol,
        favoriteStore: DydxFavoriteStoreProtocol,
    ): MutableMarketInfoStreaming {
        return MarketInfoStream(
            abacusStateManager = abacusStateManager,
            formatter = formatter,
            localizer = localizer,
            favoriteStore = favoriteStore,
        )
    }

    @Provides
    @ActivityRetainedScoped
    fun provideMarketInfoStream(
        mutableMarketInfoStream: MutableMarketInfoStreaming,
    ): MarketInfoStreaming {
        return mutableMarketInfoStream
    }

    @Provides
    @ActivityRetainedScoped
    fun provideStatsTabSelection(
        mutableStatsTabFlow: MutableStateFlow<DydxMarketStatsTabView.Selection>,
    ): Flow<DydxMarketStatsTabView.Selection> {
        return mutableStatsTabFlow
    }

    @Provides
    @ActivityRetainedScoped
    fun provideMutableStatsTabSelection(): MutableStateFlow<DydxMarketStatsTabView.Selection> {
        return MutableStateFlow(DydxMarketStatsTabView.Selection.Statistics)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideAccountTabSelection(
        mutableAccountTabFlow: MutableStateFlow<DydxMarketAccountTabView.Selection>,
    ): Flow<DydxMarketAccountTabView.Selection> {
        return mutableAccountTabFlow
    }

    @Provides
    @ActivityRetainedScoped
    fun provideMutableAccountTabSelection(): MutableStateFlow<DydxMarketAccountTabView.Selection> {
        return MutableStateFlow(DydxMarketAccountTabView.Selection.Position)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideTileSelection(
        mutableTileFlow: MutableStateFlow<DydxMarketTilesView.Tile>,
    ): Flow<DydxMarketTilesView.Tile> {
        return mutableTileFlow
    }

    @Provides
    @ActivityRetainedScoped
    fun provideMutableTileSelection(): MutableStateFlow<DydxMarketTilesView.Tile> {
        return MutableStateFlow(DydxMarketTilesView.Tile(DydxMarketTilesView.TileType.PRICE))
    }
}
