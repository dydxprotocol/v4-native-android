package exchange.dydx.trading.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

object CoroutineDispatchers {

    @Qualifier annotation class Main

    @Qualifier annotation class IO

    @Qualifier annotation class Default
}

@Module
@InstallIn(SingletonComponent::class)
object CoroutineDispatchersModule {
    @Provides @CoroutineDispatchers.Main
    fun provideMain(): CoroutineDispatcher = Dispatchers.Main

    @Provides @CoroutineDispatchers.IO
    fun provideIO(): CoroutineDispatcher = Dispatchers.IO

    @Provides @CoroutineDispatchers.Default
    fun provideDefault(): CoroutineDispatcher = Dispatchers.Default
}
