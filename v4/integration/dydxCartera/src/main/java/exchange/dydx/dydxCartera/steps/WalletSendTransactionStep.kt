package exchange.dydx.dydxCartera.steps

import android.content.Context
import android.util.Log
import exchange.dydx.cartera.CarteraConfig
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.cartera.walletprovider.EthereumTransactionRequest
import exchange.dydx.cartera.walletprovider.WalletRequest
import exchange.dydx.cartera.walletprovider.WalletTransactionRequest
import exchange.dydx.utilities.utils.AsyncStep
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class WalletSendTransactionStep(
    private val transaction: EthereumTransactionRequest,
    private val chainId: String,
    private val walletAddress: String,
    private val walletId: String?,
    private val context: Context,
    private val provider: CarteraProvider,
) : AsyncStep<String> {

    override suspend fun run(): Result<String> {
        val wallet = CarteraConfig.shared?.wallets?.firstOrNull {
            it.id == walletId
        }

        val walletRequest = WalletRequest(
            wallet = wallet,
            address = walletAddress,
            chainId = chainId,
            context = context,
            useModal = walletId == null,
        )
        val transactionRequest = WalletTransactionRequest(
            walletRequest = walletRequest,
            ethereum = transaction,
        )

        return suspendCoroutine { continuation ->
            Log.d("AsyncStep", "Sending $transaction")
            provider.send(
                request = transactionRequest,
                connected = { info ->
                    if (info == null) {
                        continuation.resume(errorEvent("Wallet not connected"))
                    }
                },
                status = { status ->
                    Log.d("AsyncStep", "Status: $status")
                },
                completion = { signed, error ->
                    if (signed != null) {
                        continuation.resume(Result.success(signed))
                    } else {
                        continuation.resume(errorEvent(error?.message ?: "Unknown error"))
                    }
                },
            )
        }
    }
}
