package exchange.dydx.trading.core

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import exchange.dydx.abacus.protocols.AbacusLocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.PlatformInfo
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.logger.DydxLogger
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.workers.DydxGlobalWorkers
import exchange.dydx.trading.integration.analytics.logging.CompositeLogging
import exchange.dydx.trading.integration.analytics.tracking.CompositeTracking
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.CachedFileLoader
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val TAG = "CoreViewModel"

@HiltViewModel
class CoreViewModel @Inject constructor(
    val router: DydxRouter,
    val loggerDeprecated: DydxLogger,
    val cosmosClient: CosmosV4WebviewClientProtocol,
    val platformInfo: PlatformInfo,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val localizer: AbacusLocalizerProtocol,
    @ApplicationContext context: Context,
    private val cachedFileLoader: CachedFileLoader,
    private val formatter: DydxFormatter,
    private val parser: ParserProtocol,
    private val tracker: Tracking,
    val logger: CompositeLogging,
    val compositeTracking: CompositeTracking,
    private val preferencesStore: SharedPreferencesStore,
) : ViewModel() {
    private var globalWorkers: DydxGlobalWorkers? = null

    init {
        globalWorkers = DydxGlobalWorkers(
            scope = viewModelScope,
            abacusStateManager = abacusStateManager,
            localizer = localizer,
            router = router,
            platformInfo = platformInfo,
            context = context,
            cachedFileLoader = cachedFileLoader,
            cosmosClient = cosmosClient,
            formatter = formatter,
            parser = parser,
            tracker = tracker,
            logger = logger,
            preferencesStore = preferencesStore,
        )
    }

    var restartCount: Int = 0

    fun start() {
        cosmosClient.initialized
            .onEach { initialized ->
                // Wait for the cosmos client to be initialized before setting the environment
                if (initialized) {
                    resetEnv()
                }
            }.launchIn(viewModelScope)
    }

    fun startWorkers() {
        globalWorkers?.start()
    }

    private fun resetEnv() {
        val envId = abacusStateManager.currentEnvironmentId.value
        abacusStateManager.setEnvironmentId(null)
        abacusStateManager.setEnvironmentId(envId)
    }
}
