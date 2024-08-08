package exchange.dydx.trading.feature.workers

import android.content.Context
import exchange.dydx.abacus.protocols.AbacusLocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.workers.globalworkers.DydxAlertsWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxApiStatusWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxCarteraConfigWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxGasTokenWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxRestrictionsWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxTransferSubaccountWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxUpdateWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxUserTrackingWorker
import exchange.dydx.trading.integration.analytics.logging.CompositeLogging
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.CachedFileLoader
import exchange.dydx.utilities.utils.SharedPreferencesStore
import exchange.dydx.utilities.utils.WorkerProtocol
import kotlinx.coroutines.CoroutineScope

class DydxGlobalWorkers(
    val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val localizer: AbacusLocalizerProtocol,
    private val router: DydxRouter,
    private val toaster: PlatformInfo,
    private val context: Context,
    private val cachedFileLoader: CachedFileLoader,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val formatter: DydxFormatter,
    private val parser: ParserProtocol,
    private val tracker: Tracking,
    private val logger: CompositeLogging,
    private val preferencesStore: SharedPreferencesStore,
) : WorkerProtocol {

    private val workers = listOf(
        DydxUpdateWorker(scope, abacusStateManager, router, context, logger),
        DydxAlertsWorker(scope, abacusStateManager, localizer, router, toaster, preferencesStore),
        DydxApiStatusWorker(scope, abacusStateManager, localizer, toaster),
        DydxRestrictionsWorker(scope, abacusStateManager, localizer, toaster),
        DydxCarteraConfigWorker(abacusStateManager, cachedFileLoader, context, logger),
        DydxTransferSubaccountWorker(scope, abacusStateManager, cosmosClient, formatter, parser, tracker, logger),
        DydxUserTrackingWorker(scope, abacusStateManager, localizer, tracker),
        DydxGasTokenWorker(preferencesStore, abacusStateManager, logger),
    )

    override var isStarted = false

    override fun start() {
        if (!isStarted) {
            isStarted = true

            workers.forEach { it.start() }
        }
    }

    override fun stop() {
        if (isStarted) {
            isStarted = false

            workers.forEach { it.stop() }
        }
    }
}
