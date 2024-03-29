package exchange.dydx.trading.feature.workers

import android.content.Context
import exchange.dydx.abacus.protocols.AbacusLocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.PlatformInfo
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.workers.globalworkers.DydxAlertsWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxApiStatusWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxCarteraConfigWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxRestrictionsWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxTransferSubaccountWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxUpdateWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxUserTrackingWorker
import exchange.dydx.trading.integration.analytics.Tracking
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.CachedFileLoader
import exchange.dydx.utilities.utils.SharedPreferencesStore
import exchange.dydx.utilities.utils.WorkerProtocol
import kotlinx.coroutines.CoroutineScope

class DydxGlobalWorkers(
    override val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val localizer: AbacusLocalizerProtocol,
    private val router: DydxRouter,
    private val platformInfo: PlatformInfo,
    private val context: Context,
    private val cachedFileLoader: CachedFileLoader,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val formatter: DydxFormatter,
    private val parser: ParserProtocol,
    private val tracker: Tracking,
    private val preferencesStore: SharedPreferencesStore,
) : WorkerProtocol {

    private val workers = listOf(
        DydxUpdateWorker(scope, abacusStateManager, router, context),
        DydxAlertsWorker(scope, abacusStateManager, localizer, router, platformInfo, preferencesStore),
        DydxApiStatusWorker(scope, abacusStateManager, localizer, platformInfo),
        DydxRestrictionsWorker(scope, abacusStateManager, localizer, platformInfo),
        DydxCarteraConfigWorker(scope, abacusStateManager, cachedFileLoader, context),
        DydxTransferSubaccountWorker(scope, abacusStateManager, cosmosClient, formatter, parser, tracker),
        DydxUserTrackingWorker(scope, abacusStateManager, localizer, tracker),
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
