package exchange.dydx.trading.feature.receipt.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.trading.feature.receipt.ReceiptType
import exchange.dydx.trading.feature.receipt.streams.ReceiptStream
import exchange.dydx.trading.feature.receipt.streams.ReceiptStreaming
import exchange.dydx.trading.feature.receipt.streams.TransferRouteSelectionInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Module
@InstallIn(ActivityRetainedComponent::class)
interface ReceiptModule {

    @Binds
    fun bindReceiptTypeFlow(
        mutableFlow: MutableStateFlow<ReceiptType?>,
    ): Flow<ReceiptType?>

    @Binds
    fun bindReceiptStream(
        receiptStream: ReceiptStream,
    ): ReceiptStreaming

    companion object {
        @Provides
        @ActivityRetainedScoped
        fun provideMutableReceiptTypeFlow(): MutableStateFlow<ReceiptType?> {
            return MutableStateFlow(null)
        }

        @Provides
        @ActivityRetainedScoped
        fun provideTransferRouteSelectionInfo(): TransferRouteSelectionInfo {
            return TransferRouteSelectionInfo()
        }
    }
}
