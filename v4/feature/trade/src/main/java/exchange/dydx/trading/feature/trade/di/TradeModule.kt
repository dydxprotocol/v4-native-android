package exchange.dydx.trading.feature.trade.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.feature.trade.streams.MutableTradeStreaming
import exchange.dydx.trading.feature.trade.streams.TradeStream
import exchange.dydx.trading.feature.trade.streams.TradeStreaming
import exchange.dydx.trading.feature.trade.tradeinput.DydxTradeInputView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Module
@InstallIn(ActivityRetainedComponent::class)
object TradeModule {
    @Provides
    @ActivityRetainedScoped
    fun provideOrderbookToggleStateFlow(
        mutableFlow: MutableStateFlow<DydxTradeInputView.OrderbookToggleState>,
    ): Flow<DydxTradeInputView.OrderbookToggleState> {
        return mutableFlow
    }

    @Provides
    @ActivityRetainedScoped
    fun provideMutableOrderbookToggleStateFlow(): MutableStateFlow<DydxTradeInputView.OrderbookToggleState> {
        return MutableStateFlow(DydxTradeInputView.OrderbookToggleState.Open)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideBottomSheetFlow(
        mutableFlow: MutableStateFlow<DydxTradeInputView.BottomSheetState?>,
    ): Flow<DydxTradeInputView.BottomSheetState?> {
        return mutableFlow
    }

    @Provides
    @ActivityRetainedScoped
    fun provideMutableBottomSheetStateFlow(): MutableStateFlow<DydxTradeInputView.BottomSheetState?> {
        return MutableStateFlow(null)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideTradeStream(
        mutableStream: MutableTradeStreaming,
    ): TradeStreaming {
        return mutableStream
    }

    @Provides
    @ActivityRetainedScoped
    fun provideMutableTradeStream(
        abacusStateManager: AbacusStateManagerProtocol,
    ): MutableTradeStreaming {
        return TradeStream(abacusStateManager)
    }
}
