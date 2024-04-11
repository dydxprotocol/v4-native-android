package exchange.dydx.trading.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.ViewModelLifecycle
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import javax.inject.Qualifier
import javax.inject.Singleton

object CoroutineScopes {
    @Qualifier annotation class App

    @Qualifier annotation class ViewModel
}

@Module
@InstallIn(SingletonComponent::class)
object AppScopeModule {
    @Provides @CoroutineScopes.App @Singleton
    fun provideScope(): CoroutineScope = MainScope()
}

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelScopeModule {
    @Provides @CoroutineScopes.ViewModel @ViewModelScoped
    fun provideScope(viewModelLifecycle: ViewModelLifecycle): CoroutineScope {
        val scope = MainScope()
        viewModelLifecycle.addOnClearedListener {
            scope.cancel()
        }
        return scope
    }
}
