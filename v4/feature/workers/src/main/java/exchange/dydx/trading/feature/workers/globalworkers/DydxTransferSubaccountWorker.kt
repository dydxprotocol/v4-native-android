package exchange.dydx.trading.feature.workers.globalworkers

import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.toJson
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.dydxstatemanager.usdcTokenDecimal
import exchange.dydx.dydxstatemanager.usdcTokenDenom
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.Logging
import exchange.dydx.utilities.utils.WorkerProtocol
import exchange.dydx.utilities.utils.jsonStringToMap
import exchange.dydx.utilities.utils.timerFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ActivityRetainedScoped
class DydxTransferSubaccountWorker @Inject constructor(
    @CoroutineScopes.App private val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val formatter: DydxFormatter,
    private val parser: ParserProtocol,
    private val tracker: Tracking,
    private val logger: Logging,
) : WorkerProtocol {

    companion object {
        const val balanceRetainAmount = 1.25
        const val rebalanceThreshold = 1.0
    }

    override var isStarted = false

    override fun start() {
        if (!isStarted) {
            isStarted = true

            combine(
                timerFlow(20.seconds),
                abacusStateManager.state.accountBalance(abacusStateManager.usdcTokenDenom),
                abacusStateManager.state.currentWallet.mapNotNull { it },
            ) { _, balance, wallet ->
                val balance = balance ?: 0.0
                if (balance > balanceRetainAmount) {
                    val depositAmount = balance.minus(balanceRetainAmount)
                    if (depositAmount <= 0) return@combine
                    val amountString = formatter.decimalLocaleAgnostic(depositAmount, abacusStateManager.usdcTokenDecimal)
                        ?: return@combine

                    depositToSubaccount(amountString, abacusStateManager.state.subaccountNumber ?: 0, wallet)
                } else if (balance < rebalanceThreshold) {
                    val withdrawAmount = balanceRetainAmount.minus(balance)
                    if (withdrawAmount <= 0) return@combine
                    val amountString = formatter.decimalLocaleAgnostic(withdrawAmount, abacusStateManager.usdcTokenDecimal)
                        ?: return@combine

                    withdrawFromSubaccount(amountString, abacusStateManager.state.subaccountNumber ?: 0, wallet)
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

    private fun depositToSubaccount(
        amountString: String,
        subaccountNumber: Int,
        wallet: DydxWalletInstance,
    ) {
        val payload: Map<String, Any> = mapOf(
            "subaccountNumber" to subaccountNumber,
            "amount" to amountString,
        )
        val paramsInJson = payload.toJson()
        cosmosClient.call(
            functionName = "deposit",
            paramsInJson = paramsInJson,
            completion = { response ->
                val trackingData = mutableMapOf(
                    "amount" to amountString,
                    "address" to wallet.cosmoAddress,
                )
                val result = response?.jsonStringToMap() ?: return@call
                val error = result["error"] as? Map<String, Any>
                val hash = parser.asString(result["hash"])
                if (hash != null) {
                    tracker.log(
                        event = "SubaccountDeposit",
                        data = trackingData,
                    )
                } else {
                    tracker.log(
                        event = "SubaccountDeposit_Failed",
                        data = trackingData.also { it["error"] = response },
                    )
                    logger.e("DydxTransferSubaccountWorker", "depositToSubaccount: $error")
                }
            },
        )
    }

    private fun withdrawFromSubaccount(
        amountString: String,
        subaccountNumber: Int,
        wallet: DydxWalletInstance,
    ) {
        val payload: Map<String, Any> = mapOf(
            "subaccountNumber" to subaccountNumber,
            "amount" to amountString,
        )
        val paramsInJson = payload.toJson()
        cosmosClient.call(
            functionName = "withdraw",
            paramsInJson = paramsInJson,
            completion = { response ->
                val trackingData = mutableMapOf(
                    "amount" to amountString,
                    "address" to wallet.cosmoAddress,
                )
                val result = response?.jsonStringToMap() ?: return@call
                val error = result["error"] as? Map<String, Any>
                val hash = parser.asString(result["hash"])
                if (hash != null) {
                    tracker.log(
                        event = "SubaccountWithdraw",
                        data = trackingData,
                    )
                } else {
                    tracker.log(
                        event = "SubaccountWithdraw_Failed",
                        data = trackingData.also { it["error"] = response },
                    )
                    logger.e("DydxTransferSubaccountWorker", "withdrawToSubaccount: $error")
                }
            },
        )
    }
}
