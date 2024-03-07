package exchange.dydx.trading.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.core.DydxRouterImpl
import exchange.dydx.trading.integration.analytics.Tracking

@Module
@InstallIn(ActivityRetainedComponent::class)
object CoreModule {

    @Provides
    @ActivityRetainedScoped
    fun provideApplicationRouter(
        @ApplicationContext androidContext: Context,
        appConfig: AppConfig,
        tracker: Tracking,
    ): DydxRouter {
        return DydxRouterImpl(
            androidContext = androidContext,
            appConfig = appConfig,
            tracker = tracker,
        )
    }
}
