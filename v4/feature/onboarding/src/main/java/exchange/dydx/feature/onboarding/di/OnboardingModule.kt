package exchange.dydx.feature.onboarding.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.dydxCartera.DydxWalletSetup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Module
@InstallIn(ActivityRetainedComponent::class)
object OnboardingModule {

    @Provides
    @ActivityRetainedScoped
    fun provideWalletSignedStatusFlow(
        flow: MutableStateFlow<DydxWalletSetup.Status.Signed?>,
    ): StateFlow<DydxWalletSetup.Status.Signed?> {
        return flow
    }

    @Provides
    @ActivityRetainedScoped
    fun provideMutableWalletSignedStatusFlow(): MutableStateFlow<DydxWalletSetup.Status.Signed?> {
        return MutableStateFlow(null)
    }
}
