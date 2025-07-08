package exchange.dydx.trading.feature.trade.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.trading.feature.trade.streams.MutableTradeStreaming
import exchange.dydx.trading.feature.trade.streams.MutableTriggerOrderStreaming
import exchange.dydx.trading.feature.trade.streams.TradeStream
import exchange.dydx.trading.feature.trade.streams.TradeStreaming
import exchange.dydx.trading.feature.trade.streams.TriggerOrderStream
import exchange.dydx.trading.feature.trade.streams.TriggerOrderStreaming
import exchange.dydx.trading.feature.trade.tradeinput.DydxTradeInputView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Module
@InstallIn(ActivityRetainedComponent::class)
interface TradeModule {
    @Binds
    fun bindOrderbookToggleStateFlow(
        mutableFlow: MutableStateFlow<DydxTradeInputView.OrderbookToggleState>,
    ): Flow<DydxTradeInputView.OrderbookToggleState>

    @Binds
    fun bindBottomSheetFlow(
        mutableFlow: MutableStateFlow<DydxTradeInputView.BottomSheetState?>,
    ): Flow<DydxTradeInputView.BottomSheetState?>

    @Binds
    fun bindTradeStream(
        mutableStream: MutableTradeStreaming,
    ): TradeStreaming

    @Binds
    fun bindMutableTradeStream(
        tradeStream: TradeStream,
    ): MutableTradeStreaming

    @Binds
    fun bindTriggerOrderStream(
        mutableStream: MutableTriggerOrderStreaming,
    ): TriggerOrderStreaming

    @Binds
    fun bindMutableTriggerOrderStream(
        triggerOrderStream: TriggerOrderStream,
    ): MutableTriggerOrderStreaming

    companion object {
        @Provides
        @ActivityRetainedScoped
        fun provideMutableOrderbookToggleStateFlow(): MutableStateFlow<DydxTradeInputView.OrderbookToggleState> {
            return MutableStateFlow(DydxTradeInputView.OrderbookToggleState.Open)
        }

        @Provides
        @ActivityRetainedScoped
        fun provideMutableBottomSheetStateFlow(): MutableStateFlow<DydxTradeInputView.BottomSheetState?> {
            return MutableStateFlow(null)
        }
    }
}
