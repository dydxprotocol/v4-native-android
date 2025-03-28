package exchange.dydx.trading.feature.workers.globalworkers

import android.app.Application
import android.content.Context
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.cartera.CarteraConfig
import exchange.dydx.cartera.PhantomWalletConfig
import exchange.dydx.cartera.WalletConnectModalConfig
import exchange.dydx.cartera.WalletConnectV2Config
import exchange.dydx.cartera.WalletProvidersConfig
import exchange.dydx.cartera.WalletSegueConfig
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.walletmodal.DydxWalletModal
import exchange.dydx.dydxstatemanager.clientState.walletmodal.DydxWalletModalStoreProtocol
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
import kotlin.String

private const val TAG = "DydxCarteraConfigWorker"

@ActivityRetainedScoped
class DydxCarteraConfigWorker @Inject constructor(
    @CoroutineScopes.App private val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val cachedFileLoader: CachedFileLoader,
    private val application: Application,
    private val logger: Logging,
    private val walletModalStore: DydxWalletModalStoreProtocol,
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

            // WalletConnect Modal's init() can't wait until the wallet ids from the env.json is loaded, so we
            // just load from the cached value.
            CarteraConfig.shared?.updateModalConfig(WalletConnectModalConfig(walletIds = walletModalStore.state.value?.walletIds))

            // Update the cached value when the environment changes
            abacusStateManager.currentEnvironmentId.onEach { _ ->
                val config = WalletProvidersConfigUtil.getWalletProvidersConfig(application, abacusStateManager)
                val walletIds = config.walletConnectModal?.walletIds
                if (!walletIds.isNullOrEmpty()) {
                    walletModalStore.update((DydxWalletModal(walletIds = walletIds)))
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

        val phantomWalletConfig = if (BuildConfig.DEBUG) {
            PhantomWalletConfig(
                callbackUrl = "https://v4.testnet.dydx.exchange/phantom",
                appUrl = "https://v4.testnet.dydx.exchange",
            )
        } else {
            abacusStateManager.environment?.walletConnection?.phantom?.callbackUrl?.let {
                PhantomWalletConfig(
                    callbackUrl = it,
                    appUrl = abacusStateManager.deploymentUri,
                )
            }
        }

        return WalletProvidersConfig(
            walletConnectV1 = null,
            walletConnectV2 = walletConnectV2Config,
            walletConnectModal = walletConnectModalConfig,
            walletSegue = walletSegueConfig,
            phantomWallet = phantomWalletConfig,
        )
    }
}
