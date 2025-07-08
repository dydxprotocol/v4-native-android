package exchange.dydx.trading.feature.transfer.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.trading.feature.shared.DydxScreenResult
import exchange.dydx.trading.feature.transfer.DydxTransferError
import exchange.dydx.trading.feature.transfer.DydxTransferSectionsView
import exchange.dydx.trading.feature.transfer.search.DydxTransferSearchParam
import exchange.dydx.trading.feature.transfer.utils.DydxTransferInstanceStore
import exchange.dydx.trading.feature.transfer.utils.DydxTransferInstanceStoring
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Module
@InstallIn(ActivityRetainedComponent::class)
interface TransferModule {
    @Binds
    fun bindSections(
        mutableFlow: MutableStateFlow<DydxTransferSectionsView.Selection>,
    ): Flow<DydxTransferSectionsView.Selection>

    @Binds
    fun bindDydxTransferSearchParam(
        mutableFlow: MutableStateFlow<DydxTransferSearchParam?>,
    ): Flow<DydxTransferSearchParam?>

    @Binds
    fun bindDydxTransferInstanceStoring(
        dydxTransferInstanceStore: DydxTransferInstanceStore
    ): DydxTransferInstanceStoring

    @Binds
    fun bindError(
        mutableFlow: MutableStateFlow<DydxTransferError?>,
    ): StateFlow<DydxTransferError?>

    @Binds
    fun bindScreenResult(
        mutableFlow: MutableStateFlow<DydxScreenResult?>,
    ): Flow<DydxScreenResult?>

    companion object {
        @Provides
        @ActivityRetainedScoped
        fun provideMutableDydxTransferSearchParam(): MutableStateFlow<DydxTransferSearchParam?> {
            return MutableStateFlow(null)
        }

        @Provides
        @ActivityRetainedScoped
        fun providateMutableSections(): MutableStateFlow<DydxTransferSectionsView.Selection> {
            return MutableStateFlow(DydxTransferSectionsView.Selection.Deposit)
        }

        @Provides
        @ActivityRetainedScoped
        fun provideMutableError(): MutableStateFlow<DydxTransferError?> {
            return MutableStateFlow(null)
        }

        @Provides
        @ActivityRetainedScoped
        fun provideMutableScreenResult(): MutableStateFlow<DydxScreenResult?> {
            return MutableStateFlow(null)
        }
    }
}
