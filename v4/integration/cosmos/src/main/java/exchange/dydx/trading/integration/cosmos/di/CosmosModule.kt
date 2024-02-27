package exchange.dydx.trading.integration.cosmos.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import exchange.dydx.trading.common.DydxException

private const val TAG = "CosmosModule"

@Module
@InstallIn(ActivityRetainedComponent::class)
object CosmosModule {

//    @ActivityRetainedScoped
//    @Provides
//    fun provideCosmosWebviewClient(@ApplicationContext context: Context, json: Json, starkex: StarkexLib): CosmosV4WebviewClientProtocol {
//        return CosmosV4ClientWebview(context)
//    }
//
//    @Provides
//    @Singleton
//    fun provideCosmosClient(@ApplicationContext appContext: Context): CosmosV4ClientProtocol =
//        CosmosV4ClientWebview(appContext)
}

class CosmosException(cause: Throwable? = null, s: String) : DydxException(s, cause)
