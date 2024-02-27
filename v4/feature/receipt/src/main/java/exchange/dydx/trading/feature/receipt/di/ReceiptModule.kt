package exchange.dydx.trading.feature.receipt.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.feature.receipt.ReceiptType
import exchange.dydx.trading.feature.receipt.streams.ReceiptStream
import exchange.dydx.trading.feature.receipt.streams.ReceiptStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Module
@InstallIn(ActivityRetainedComponent::class)
object ReceiptModule {

    @Provides
    @ActivityRetainedScoped
    fun provideReceiptTypeFlow(
        mutableFlow: MutableStateFlow<ReceiptType?>,
    ): Flow<ReceiptType?> {
        return mutableFlow
    }

    @Provides
    @ActivityRetainedScoped
    fun provideMutableReceiptTypeFlow(): MutableStateFlow<ReceiptType?> {
        return MutableStateFlow(null)
    }

    @Provides
    @ActivityRetainedScoped
    fun providesReceiptStream(
        abacusStateManager: AbacusStateManagerProtocol,
        receiptTypeFlow: Flow<@JvmSuppressWildcards ReceiptType?>,
    ): ReceiptStreaming {
        return ReceiptStream(
            abacusStateManager = abacusStateManager,
            receiptTypeFlow = receiptTypeFlow,
        )
    }
}
