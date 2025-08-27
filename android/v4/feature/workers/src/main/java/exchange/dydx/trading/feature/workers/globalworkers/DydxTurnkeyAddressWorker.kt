package exchange.dydx.trading.feature.workers.globalworkers

import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.depositaddresses.DepositAddresses
import exchange.dydx.dydxstatemanager.clientState.depositaddresses.DydxDepositAddressesStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.integration.react.TurnkeyReactBridge
import exchange.dydx.utilities.utils.Logging
import exchange.dydx.utilities.utils.WorkerProtocol
import exchange.dydx.utilities.utils.delayFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

private val TAG = "DydxTurnkeyAddressWorker"

@ActivityRetainedScoped
class DydxTurnkeyAddressWorker @Inject constructor(
    @CoroutineScopes.App private val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val turnkeyReactBridge: TurnkeyReactBridge,
    private val depositAddressesStateManager: DydxDepositAddressesStateManagerProtocol,
    private val logger: Logging,
) : WorkerProtocol {
    override var isStarted = false

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun start() {
        if (!isStarted) {
            isStarted = true

            delayFlow(duration = 2.seconds)
                .flatMapLatest {
                    abacusStateManager.state.walletState
                }
                .onEach { walletState ->
                    val currentWallet = walletState?.currentWallet ?: return@onEach
                    if (currentWallet.walletId == "turnkey") {
                        fetchAndUpdateTurnkeyAddress(currentWallet)
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

    private fun fetchAndUpdateTurnkeyAddress(wallet: DydxWalletInstance) {
        val dydxAddress = wallet.cosmoAddress ?: return
        val indexerUrl = abacusStateManager.environment?.endpoints?.indexers?.firstOrNull()?.api
            ?: return

        turnkeyReactBridge.fetchDepositAddresses(dydxAddress = dydxAddress, indexerUrl = indexerUrl) { result ->
            try {
                val addresses = Json.decodeFromString<DepositAddresses>(result)
                if (!addresses.evmAddress.isNullOrEmpty()) {
                    depositAddressesStateManager.update(addresses)
                }
            } catch (e: Exception) {
                logger.e(TAG, "Turnkey failed to decode address: $e")
                return@fetchDepositAddresses
            }
        }
    }
}
