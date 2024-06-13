package exchange.dydx.trading.feature.workers.globalworkers

import exchange.dydx.cartera.CarteraConfig
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.BuildConfig
import exchange.dydx.utilities.utils.CachedFileLoader
import exchange.dydx.utilities.utils.Logging
import exchange.dydx.utilities.utils.WorkerProtocol

private const val TAG = "DydxCarteraConfigWorker"

class DydxCarteraConfigWorker(
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val cachedFileLoader: CachedFileLoader,
    private val context: android.content.Context,
    private val logger: Logging,
) : WorkerProtocol {
    override var isStarted = false

    override fun start() {
        if (!isStarted) {
            isStarted = true

            val filePath = "configs/wallets.json"
            val url = if (BuildConfig.DEBUG) null else abacusStateManager.deploymentUri + "/" + filePath
            cachedFileLoader.loadString(filePath, url) { jsonString ->
                jsonString?.let {
                    CarteraConfig.shared?.registerWallets(context, jsonString)
                } ?: run {
                    logger.e(TAG, "Failed to load wallets.json")
                }
            }
        }
    }

    override fun stop() {
        if (isStarted) {
            isStarted = false
        }
    }
}
