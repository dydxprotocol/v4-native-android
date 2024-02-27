package exchange.dydx.trading.feature.transfer.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
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
class TransferModule {
    @Provides
    @ActivityRetainedScoped
    fun provideSections(
        mutableFlow: MutableStateFlow<DydxTransferSectionsView.Selection>,
    ): Flow<DydxTransferSectionsView.Selection> {
        return mutableFlow
    }

    @Provides
    @ActivityRetainedScoped
    fun providateMutableSections(): MutableStateFlow<DydxTransferSectionsView.Selection> {
        return MutableStateFlow(DydxTransferSectionsView.Selection.Deposit)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideDydxTransferSearchParam(
        mutableFlow: MutableStateFlow<DydxTransferSearchParam?>,
    ): Flow<DydxTransferSearchParam?> {
        return mutableFlow
    }

    @Provides
    @ActivityRetainedScoped
    fun provideMutableDydxTransferSearchParam(): MutableStateFlow<DydxTransferSearchParam?> {
        return MutableStateFlow(null)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideDydxTransferInstanceStoring(
        abausStateManager: AbacusStateManagerProtocol,
        parser: ParserProtocol,
    ): DydxTransferInstanceStoring {
        return DydxTransferInstanceStore(abausStateManager, parser)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideError(
        mutableFlow: MutableStateFlow<DydxTransferError?>,
    ): StateFlow<DydxTransferError?> {
        return mutableFlow
    }

    @Provides
    @ActivityRetainedScoped
    fun provideMutableError(): MutableStateFlow<DydxTransferError?> {
        return MutableStateFlow(null)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideScreenResult(
        mutableFlow: MutableStateFlow<DydxScreenResult?>,
    ): Flow<DydxScreenResult?> {
        return mutableFlow
    }

    @Provides
    @ActivityRetainedScoped
    fun provideMutableScreenResult(): MutableStateFlow<DydxScreenResult?> {
        return MutableStateFlow(null)
    }
}
