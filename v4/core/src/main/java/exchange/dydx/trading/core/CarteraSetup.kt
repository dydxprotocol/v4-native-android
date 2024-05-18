package exchange.dydx.trading.core

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import exchange.dydx.cartera.CarteraConfig
import exchange.dydx.cartera.WalletConnectV2Config
import exchange.dydx.cartera.WalletProvidersConfig
import exchange.dydx.cartera.WalletSegueConfig
import exchange.dydx.utilities.utils.Logging

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
            walletProvidersConfig = WalletProvidersConfig(
                null,
                null,
                null,
            ),
            application = activity.application,
            launcher = launcher,
        )

        // DydxCarteraConfigWorker will fetch the wallet config from the server and update the config.

        // For debuggging
        // CarteraConfig.shared?.updateConfig(WalletProvidersConfigUtil.getWalletProvidersConfig())
    }
}

object WalletProvidersConfigUtil {
    fun getWalletProvidersConfig(): WalletProvidersConfig {
        val walletConnectV2Config = WalletConnectV2Config(
            "47559b2ec96c09aed9ff2cb54a31ab0e",
            "dYdX v4",
            "dYdX Trading App",
            "https://v4.testnet.dydx.exchange/",
            listOf<String>("https://v4.testnet.dydx.exchange/logos/dydx-x.png"),
        )

        val walletSegueConfig = WalletSegueConfig(
            "https://v4.testnet.dydx.exchange/walletsegue",
        )

        return WalletProvidersConfig(
            null,
            walletConnectV2Config,
            walletSegueConfig,
        )
    }
}
