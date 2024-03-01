package exchange.dydx.trading.feature.workers.globalworkers

import android.util.Log
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.toJson
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.dydxstatemanager.usdcTokenDecimal
import exchange.dydx.dydxstatemanager.usdcTokenInfo
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.integration.analytics.Tracking
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.WorkerProtocol
import exchange.dydx.utilities.utils.jsonStringToMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull

class DydxTransferSubaccountWorker(
    override val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val formatter: DydxFormatter,
    private val parser: ParserProtocol,
    private val tracker: Tracking,
) : WorkerProtocol {

    companion object {
        const val balanceRetainAmount = 0.25
    }

    override var isStarted = false

    override fun start() {
        if (!isStarted) {
            isStarted = true

            combine(
                abacusStateManager.state.accountBalance(abacusStateManager.environment?.usdcTokenInfo?.denom)
                    .filter { balance ->
                        balance != null && balance > balanceRetainAmount
                    },
                abacusStateManager.state.currentWallet.mapNotNull { it },
                abacusStateManager.state.selectedSubaccount,
            ) { balance, wallet, selectedSubaccount ->
                val subaccountNumber: Int = selectedSubaccount?.subaccountNumber ?: 0
                val depositAmount = balance?.minus(balanceRetainAmount) ?: 0.0
                if (depositAmount <= 0) return@combine
                val amountString = formatter.raw(depositAmount, abacusStateManager.usdcTokenDecimal)
                    ?: return@combine

                depositToSubaccount(amountString, subaccountNumber, wallet)
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
                val trackingData = mapOf(
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
                        data = trackingData,
                    )
                    Log.e("DydxTransferSubaccountWorker", "depositToSubaccount: $error")
                }
            },
        )
    }
}
