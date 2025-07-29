package exchange.dydx.trading.feature.workers.globalworkers

import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.state.manager.ChainRpcMap
import exchange.dydx.dydxCartera.solana.SolanaInteractor
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.featureflags.DydxBoolFeatureFlag
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.feature.shared.TransferChain
import exchange.dydx.trading.feature.shared.TransferToken
import exchange.dydx.trading.feature.shared.TransferTokenDetails
import exchange.dydx.trading.feature.shared.TransferTokenInfo
import exchange.dydx.utilities.utils.Logging
import exchange.dydx.utilities.utils.WorkerProtocol
import exchange.dydx.web3.EthereumInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

private const val TAG = "DydxTransferTokensWorker"

@ActivityRetainedScoped
class DydxTransferTokensWorker @Inject constructor(
    @CoroutineScopes.App private val scope: CoroutineScope,
    private val transferTokenDetails: TransferTokenDetails,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val logger: Logging,
    private val featureFlags: DydxFeatureFlags,
) : WorkerProtocol {
    private var solanaInteractor: SolanaInteractor? = null
    private var ethereumInteractors = mutableMapOf<String, EthereumInteractor>()

    override var isStarted = false

    override fun start() {
        if (!isStarted) {
            isStarted = true
        }

        combine(
            abacusStateManager.state.configs.mapNotNull { it?.rpcMap },
            abacusStateManager.state.currentWallet.mapNotNull { it },
            transferTokenDetails.infos.filter { it.size > 0 }.take(1),
            transferTokenDetails.refreshCounter,
        ) { rpcMap, currentWallet, infos, _ ->
            val ethereumAddress = currentWallet.ethereumAddress
                ?: return@combine null // skip if no ethereum address is available

            if (solanaInteractor == null) {
                val rpcUrl = abacusStateManager.environment?.endpoints?.solanaRpcUrl
                    ?: if (abacusStateManager.state.isMainNet ||
                        featureFlags.isFeatureEnabled(DydxBoolFeatureFlag.force_mainnet)
                    ) {
                        SolanaInteractor.mainnetUrl
                    } else {
                        SolanaInteractor.devnetUrl
                    }
                solanaInteractor = SolanaInteractor(rpcUrl = rpcUrl)
            }

            infos.forEach { token ->
                if (currentWallet.walletId == "phantom-wallet") {
                    loadSolanaTokenInfo(info = token, publicKey = ethereumAddress)
                } else {
                    loadEthTokenInfo(info = token, rpcMap = rpcMap, sourceAddress = ethereumAddress)
                }
            }
        }
            .launchIn(scope)

        // Set default token
        combine(
            transferTokenDetails.infos,
            abacusStateManager.state.currentWallet.mapNotNull { it },
        ) { tokens, currentWallet ->
            val default = if (currentWallet.walletId == "phantom-wallet") {
                tokens.firstOrNull { it.chain == TransferChain.Solana && it.token == TransferToken.USDC }
            } else {
                tokens.firstOrNull()
            }
            default?.let {
                transferTokenDetails.defaultToken.value = it
            }
        }
            .launchIn(scope)
    }

    override fun stop() {
        if (isStarted) {
            isStarted = false
        }
    }

    private fun loadSolanaTokenInfo(info: TransferTokenInfo, publicKey: String) {
        if (info.chain != TransferChain.Solana) {
            var info = info
            info.amount = 0.0
            info.usdcAmount = 0.0
            transferTokenDetails.update(info = info)
            return
        }

        if (info.token == TransferToken.SOL) {
            CoroutineScope(Dispatchers.IO).launch {
                val balance = solanaInteractor?.getBalance(publicKey = publicKey)
                if (balance != null) {
                    val tokenAmount = balance / 10.0.pow(info.decimals.toDouble())
                    val info = info
                    info.amount = tokenAmount
                    scope.launch {
                        transferTokenDetails.update(info = info)
                    }
                } else {
                    logger.e(TAG, "Failed to fetch token amount (getBalance) $balance")
                }
            }
        } else if (info.token == TransferToken.USDC) {
            CoroutineScope(Dispatchers.IO).launch {
                val tokenAmount = solanaInteractor?.getTokenBalance(
                    publicKey = publicKey,
                    tokenAddress = info.tokenAddress,
                ) ?: 0.0
                val info = info
                info.amount = tokenAmount
                info.usdcAmount = tokenAmount
                scope.launch {
                    transferTokenDetails.update(info = info)
                }
            }
        }
    }

    private fun loadEthTokenInfo(
        info: TransferTokenInfo,
        rpcMap: ChainRpcMap,
        sourceAddress: String
    ) {
        if (info.chain == TransferChain.Solana) {
            // Solana tokens are not supported in this function
            var info = info
            info.amount = 0.0
            info.usdcAmount = 0.0
            transferTokenDetails.update(info = info)
            return
        }

        val rpcInfo = rpcMap[info.chainId] ?: return

        val ethereumInteractor = ethereumInteractors[rpcInfo.rpcUrl] ?: EthereumInteractor(rpcInfo.rpcUrl)
        ethereumInteractors[rpcInfo.rpcUrl] = ethereumInteractor

        if (info.tokenAddress == "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE") {
            ethereumInteractor.ethGetBalance(sourceAddress) { error, balance ->
                if (error == null && balance != null) {
                    val tokenAmount = balance.toDouble() / 10.0.pow(info.decimals.toDouble())
                    val info = info
                    info.amount = tokenAmount
                    transferTokenDetails.update(info = info)
                } else {
                    logger.e(TAG, "Failed to fetch token amount (ethGetBalance) $error")
                }
            }
        } else {
            ethereumInteractor.erc20TokenGetBalance(
                accountAddress = sourceAddress,
                tokenAddress = info.tokenAddress,
            ) { error, balance ->
                if (error == null && balance != null) {
                    val tokenAmount = balance.toDouble() / 10.0.pow(info.decimals.toDouble())
                    val info = info
                    info.amount = tokenAmount
                    info.usdcAmount = tokenAmount
                    transferTokenDetails.update(info = info)
                } else {
                    logger.e(TAG, "Failed to fetch token amount (erc20TokenGetBalance) $error")
                }
            }
        }
    }
}
