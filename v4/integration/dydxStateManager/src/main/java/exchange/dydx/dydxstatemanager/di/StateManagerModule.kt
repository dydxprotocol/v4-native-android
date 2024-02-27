package exchange.dydx.dydxstatemanager.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
object StateManagerModule {
//    @ActivityRetainedScoped
//    @Provides
//    fun provideAbacusAppStateMachine(): AppStateMachine {
//        return AppStateMachine()
//    }
}
