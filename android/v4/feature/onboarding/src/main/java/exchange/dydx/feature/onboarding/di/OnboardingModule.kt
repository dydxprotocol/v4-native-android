package exchange.dydx.feature.onboarding.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.ViewModelScoped
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxCartera.DydxWalletSetup
import exchange.dydx.dydxCartera.v4.DydxV4WalletSetup
import exchange.dydx.trading.integration.cosmos.CosmosV4ClientProtocol
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Module
@InstallIn(ActivityRetainedComponent::class)
interface OnboardingModule {

    @Binds
    fun bindWalletSignedStatusFlow(
        flow: MutableStateFlow<DydxWalletSetup.Status.Signed?>,
    ): StateFlow<DydxWalletSetup.Status.Signed?>

    companion object {
        @Provides
        @ActivityRetainedScoped
        fun provideMutableWalletSignedStatusFlow(): MutableStateFlow<DydxWalletSetup.Status.Signed?> {
            return MutableStateFlow(null)
        }
    }
}

@Module
@InstallIn(ViewModelComponent::class)
interface OnboardingViewModelModule {
    companion object {
        @Provides
        @ViewModelScoped
        fun provideWalletSetup(
            cosmosV4Client: CosmosV4ClientProtocol,
            parser: ParserProtocol,
            logger: Logging,
            @ApplicationContext context: Context,
        ): DydxV4WalletSetup {
            return DydxV4WalletSetup(context, cosmosV4Client, parser, logger)
        }
    }
}
