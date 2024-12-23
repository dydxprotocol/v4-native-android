package exchange.dydx.trading.feature.workers.globalworkers

import android.app.Application
import android.content.Context
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.cartera.CarteraConfig
import exchange.dydx.cartera.WalletConnectModalConfig
import exchange.dydx.cartera.WalletConnectV2Config
import exchange.dydx.cartera.WalletProvidersConfig
import exchange.dydx.cartera.WalletSegueConfig
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.BuildConfig
import exchange.dydx.trading.common.R
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.utilities.utils.CachedFileLoader
import exchange.dydx.utilities.utils.Logging
import exchange.dydx.utilities.utils.WorkerProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val TAG = "DydxCarteraConfigWorker"

@ActivityRetainedScoped
class DydxCarteraConfigWorker @Inject constructor(
    @CoroutineScopes.App private val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val cachedFileLoader: CachedFileLoader,
    private val application: Application,
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
                    CarteraConfig.shared?.registerWallets(application, jsonString)
                } ?: run {
                    logger.e(TAG, "Failed to load wallets.json")
                }
            }

            abacusStateManager.currentEnvironmentId.onEach { _ ->
                val config = WalletProvidersConfigUtil.getWalletProvidersConfig(application, abacusStateManager)
                val modalConfig = config.walletConnectModal
                if (modalConfig != null) {
                    CarteraConfig.shared?.updateModalConfig(modalConfig)
                }
            }
                .launchIn(scope)
        }
    }

    override fun stop() {
        if (isStarted) {
            isStarted = false
        }
    }
}

object WalletProvidersConfigUtil {
    fun getWalletProvidersConfig(appContext: Context, abacusStateManager: AbacusStateManagerProtocol): WalletProvidersConfig {
        val appHostUrl = "https://" + appContext.getString(R.string.app_web_host)
        val walletConnectV2Config = WalletConnectV2Config(
            projectId = appContext.getString(R.string.wallet_connect_project_id),
            clientName = appContext.getString(R.string.app_name),
            clientDescription = appContext.getString(R.string.wallet_connect_description),
            clientUrl = appHostUrl,
            iconUrls = listOf<String>(appHostUrl + appContext.getString(R.string.wallet_connect_logo)),
        )

        val walletSegueConfig = WalletSegueConfig(
            callbackUrl = appHostUrl + appContext.getString(R.string.wallet_segue_callback),
        )

        val walletIds = abacusStateManager.environment?.walletConnection?.walletConnect?.v2?.wallets?.android
        val walletConnectModalConfig = WalletConnectModalConfig(
            walletIds = walletIds?.toList(),
        )

        return WalletProvidersConfig(
            walletConnectV1 = null,
            walletConnectV2 = walletConnectV2Config,
            walletConnectModal = walletConnectModalConfig,
            walletSegue = walletSegueConfig,
        )
    }
}
