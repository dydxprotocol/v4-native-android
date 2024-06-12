package exchange.dydx.trading.feature.workers.globalworkers

import exchange.dydx.abacus.state.manager.GasToken
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.feature.shared.PreferenceKeys
import exchange.dydx.utilities.utils.Logging
import exchange.dydx.utilities.utils.SharedPreferencesStore
import exchange.dydx.utilities.utils.WorkerProtocol

private const val TAG = "DydxGasTokenWorker"

class DydxGasTokenWorker(
    private val preferencesStore: SharedPreferencesStore,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val logger: Logging,
) : WorkerProtocol {
    override var isStarted = false

    override fun start() {
        if (!isStarted) {
            isStarted = true

            val gasToken = preferencesStore.read(PreferenceKeys.GasToken, defaultValue = "USDC")
            try {
                abacusStateManager.setGasToken(GasToken.valueOf(gasToken))
            } catch (e: IllegalArgumentException) {
                logger.e(TAG, "Invalid gas token: $gasToken")
            }
        }
    }

    override fun stop() {
        if (isStarted) {
            isStarted = false
        }
    }
}
