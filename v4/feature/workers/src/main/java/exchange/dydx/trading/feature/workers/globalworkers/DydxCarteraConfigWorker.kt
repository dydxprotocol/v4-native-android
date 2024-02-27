package exchange.dydx.trading.feature.workers.globalworkers

import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.cartera.CarteraConfig
import exchange.dydx.cartera.WalletConnectV2Config
import exchange.dydx.cartera.WalletProvidersConfig
import exchange.dydx.cartera.WalletSegueConfig
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.BuildConfig
import exchange.dydx.utilities.utils.CachedFileLoader
import exchange.dydx.utilities.utils.WorkerProtocol
import kotlinx.coroutines.CoroutineScope

class DydxCarteraConfigWorker(
    override val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val cachedFileLoader: CachedFileLoader,
    private val context: android.content.Context,
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
                }
            }

            abacusStateManager.environment?.let {
                configureCartera(it)
            }
        }
    }

    override fun stop() {
        if (isStarted) {
            isStarted = false
        }
    }

    private fun configureCartera(environment: V4Environment) {
        val walletProviderConfig = WalletProvidersConfig(
            walletConnectV1 = null,
            walletConnectV2 = environment.walletConnectV2Config(abacusStateManager),
            walletSegue = environment.walletSegueConfig(),
        )

        CarteraConfig.shared?.updateConfig(
            walletProviderConfig,
        )
    }
}

private fun V4Environment.walletConnectV2Config(
    abacusStateManager: AbacusStateManagerProtocol,
): WalletConnectV2Config? {
    val projectId = walletConnection?.walletConnect?.v2?.projectId ?: return null
    val clientName = walletConnection?.walletConnect?.client?.name ?: return null
    val clientDescription = walletConnection?.walletConnect?.client?.description ?: return null
    val deploymentUri = abacusStateManager.deploymentUri
    val iconUrls = listOf(walletConnection?.walletConnect?.client?.iconUrl).filterNotNull()

    return WalletConnectV2Config(
        projectId = projectId,
        clientName = clientName,
        clientDescription = clientDescription,
        clientUrl = deploymentUri,
        iconUrls = iconUrls,
    )
}

private fun V4Environment.walletSegueConfig(): WalletSegueConfig? {
    val callbackUrl = walletConnection?.walletSegue?.callbackUrl ?: return null

    return WalletSegueConfig(
        callbackUrl = callbackUrl,
    )
}
