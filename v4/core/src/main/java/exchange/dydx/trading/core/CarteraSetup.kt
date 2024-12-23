package exchange.dydx.trading.core

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import exchange.dydx.cartera.CarteraConfig
import exchange.dydx.cartera.WalletConnectionType
import exchange.dydx.cartera.walletprovider.providers.WalletConnectModalProvider
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.feature.workers.globalworkers.WalletProvidersConfigUtil.getWalletProvidersConfig
import exchange.dydx.utilities.utils.Logging

object CarteraSetup {

    private const val TAG = "CarteraSetup"

    fun run(
        activity: FragmentActivity,
        logger: Logging,
        abacusStateManager: AbacusStateManagerProtocol,
    ) {
        try {
            setUpCartera(activity, abacusStateManager)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to set up cartera")
        }
    }

    fun setUpNavHostController(nav: NavHostController) {
        // Need to set the nav controller for the WalletConnectModalProvider
        val modal = CarteraConfig.shared?.getProvider(WalletConnectionType.WalletConnectModal) as? WalletConnectModalProvider
        modal?.nav = nav
    }

    private fun setUpCartera(activity: FragmentActivity, abacusStateManager: AbacusStateManagerProtocol) {
        val launcher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data ?: return@registerForActivityResult
            CarteraConfig.handleResponse(uri)
        }

        CarteraConfig.shared = CarteraConfig(
            walletProvidersConfig = getWalletProvidersConfig(activity.applicationContext, abacusStateManager),
            application = activity.application,
            launcher = launcher,
        )

        // DydxCarteraConfigWorker will fetch the wallet config from the server and update the config.

        // For debuggging
        // CarteraConfig.shared?.updateConfig(WalletProvidersConfigUtil.getWalletProvidersConfig())
    }
}
