package exchange.dydx.dydxCartera.steps

import android.content.Context
import exchange.dydx.cartera.CarteraConfig
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.cartera.walletprovider.EthereumTransactionRequest
import exchange.dydx.cartera.walletprovider.WalletRequest
import exchange.dydx.cartera.walletprovider.WalletTransactionRequest
import exchange.dydx.utilities.utils.AsyncStep
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class WalletSendTransactionStep(
    private val ethereum: EthereumTransactionRequest?,
    private val solana: ByteArray?,
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
            ethereum = ethereum,
            solana = solana,
        )

        return suspendCoroutine { continuation ->
            Timber.tag("AsyncStep").d("Sending $ethereum")
            provider.send(
                request = transactionRequest,
                connected = { info ->
                    if (info == null) {
                        continuation.resume(errorEvent("Wallet not connected"))
                    }
                },
                status = { status ->
                    Timber.tag("AsyncStep").d("Status: $status")
                },
                completion = { signed, error ->
                    if (signed != null) {
                        continuation.resume(Result.success(signed))
                    } else {
                        if (provider.walletStatus?.connectedWallet?.peerName == "MetaMask Wallet" &&
                            error?.message == "User rejected."
                        ) {
                            // MetaMask wallet will send a "User rejected" response when switching chain... let's catch it and resend
                            provider.send(
                                request = transactionRequest,
                                connected = { info ->
                                    if (info == null) {
                                        continuation.resume(errorEvent("Wallet not connected"))
                                    }
                                },
                                status = { status ->
                                    Timber.tag("AsyncStep").d("Status: $status")
                                },
                                completion = { signed, error ->
                                    if (signed != null) {
                                        continuation.resume(Result.success(signed))
                                    } else {
                                        continuation.resume(errorEvent(error?.message ?: "Unknown error"))
                                    }
                                },
                            )
                        } else {
                            continuation.resume(errorEvent(error?.message ?: "Unknown error"))
                        }
                    }
                },
            )
        }
    }
}
