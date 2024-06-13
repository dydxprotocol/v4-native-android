package exchange.dydx.trading.core

import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import exchange.dydx.cartera.CarteraConfig
import exchange.dydx.cartera.WalletConnectV2Config
import exchange.dydx.cartera.WalletProvidersConfig
import exchange.dydx.cartera.WalletSegueConfig
import exchange.dydx.utilities.utils.Logging
import exchange.dydx.trading.common.R
import exchange.dydx.trading.core.WalletProvidersConfigUtil.getWalletProvidersConfig

object CarteraSetup {

    private const val TAG = "CarteraSetup"

    fun run(
        activity: FragmentActivity,
        logger: Logging,
    ) {
        try {
            setUpCartera(activity)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to set up cartera")
        }
    }

    private fun setUpCartera(activity: FragmentActivity) {
        val launcher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data ?: return@registerForActivityResult
            CarteraConfig.handleResponse(uri)
        }

        CarteraConfig.shared = CarteraConfig(
            walletProvidersConfig = getWalletProvidersConfig(activity.applicationContext),
            application = activity.application,
            launcher = launcher,
        )

        // DydxCarteraConfigWorker will fetch the wallet config from the server and update the config.

        // For debuggging
        // CarteraConfig.shared?.updateConfig(WalletProvidersConfigUtil.getWalletProvidersConfig())
    }
}

object WalletProvidersConfigUtil {
    fun getWalletProvidersConfig(appContext: Context): WalletProvidersConfig {
        val appHostUrl = "https://" +  appContext.getString(R.string.app_web_host)
        val walletConnectV2Config = WalletConnectV2Config(
            projectId = appContext.getString(R.string.wallet_connect_project_id),
            clientName = appContext.getString(R.string.app_name),
            clientDescription = appContext.getString(R.string.wallet_connect_description),
            clientUrl = appHostUrl,
            iconUrls = listOf<String>(appHostUrl + appContext.getString(R.string.wallet_connect_logo))
        )

        val walletSegueConfig = WalletSegueConfig(
            callbackUrl = appHostUrl + appContext.getString(R.string.wallet_segue_callback)
        )

        return WalletProvidersConfig(
            walletConnectV1 = null,
            walletConnectV2 = walletConnectV2Config,
            walletSegue = walletSegueConfig,
        )
    }
}
