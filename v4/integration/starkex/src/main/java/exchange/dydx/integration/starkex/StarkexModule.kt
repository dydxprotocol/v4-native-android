package exchange.dydx.integration.starkex

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped

private const val TAG = "StarkexModule"

@Module
@InstallIn(ActivityRetainedComponent::class)
object StarkexModule {

    @ActivityRetainedScoped
    @Provides
    fun provideStarkexLib(@ApplicationContext context: Context): StarkexLib {
        return StarkexLib(context)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideStarkexEth(@ApplicationContext context: Context): StarkexEth {
        return StarkexEth(context)
    }
}
