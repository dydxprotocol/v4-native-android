package exchange.dydx.trading.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.core.DydxRouterImpl

@Module
@InstallIn(ActivityRetainedComponent::class)
interface CoreModule {

    @Binds
    fun bindDydxRouter(dydxRouterImpl: DydxRouterImpl): DydxRouter
}
