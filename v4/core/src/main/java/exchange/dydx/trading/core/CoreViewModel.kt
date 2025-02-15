package exchange.dydx.trading.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.logger.DydxLogger
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.integration.analytics.logging.CompositeLogging
import exchange.dydx.trading.integration.analytics.tracking.CompositeTracking
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.WorkerProtocol
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val TAG = "CoreViewModel"

@HiltViewModel
class CoreViewModel @Inject constructor(
    val router: DydxRouter,
    val loggerDeprecated: DydxLogger,
    val cosmosClient: CosmosV4WebviewClientProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    val logger: CompositeLogging,
    val compositeTracking: CompositeTracking,
    private val globalWorkers: List<@JvmSuppressWildcards WorkerProtocol>,
) : ViewModel() {

    var restartCount: Int = 0

    fun start() {
        cosmosClient.initialized
            .onEach { initialized ->
                // Wait for the cosmos client to be initialized before setting the environment
                if (initialized) {
                    abacusStateManager.startAbacus()
                }
            }.launchIn(viewModelScope)
    }

    fun startWorkers() {
        globalWorkers.forEach { it.start() }
    }
}
