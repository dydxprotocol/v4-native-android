package exchange.dydx.dydxCartera.steps

import android.content.Context
import exchange.dydx.cartera.CarteraConfig
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.cartera.walletprovider.EthereumTransactionRequest
import exchange.dydx.cartera.walletprovider.WalletRequest
import exchange.dydx.cartera.walletprovider.WalletTransactionRequest
import exchange.dydx.utilities.utils.AsyncEvent
import exchange.dydx.utilities.utils.AsyncStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class WalletSendTransactionStep(
    private val transaction: EthereumTransactionRequest,
    private val chainId: String,
    private val walletAddress: String,
    private val walletId: String?,
    private val context: Context,
    private val provider: CarteraProvider,
) : AsyncStep<Unit, String> {

    private val eventFlow: MutableStateFlow<AsyncEvent<Unit, String>> = MutableStateFlow(AsyncEvent.Progress(Unit))

    override fun run(): Flow<AsyncEvent<Unit, String>> {
        val wallet = CarteraConfig.shared?.wallets?.firstOrNull { it.id == walletId } ?: CarteraConfig.shared?.wallets?.firstOrNull() ?: return flowOf(invalidInputEvent)
        val walletRequest = WalletRequest(
            wallet = wallet,
            address = walletAddress,
            chainId = chainId,
            context = context,
        )
        val transaction = WalletTransactionRequest(
            walletRequest = walletRequest,
            ethereum = transaction,
        )

        provider.send(
            request = transaction,
            connected = { info ->
                if (info == null) {
                    eventFlow.value = errorEvent("Wallet not connected")
                }
            },
            completion = { signed, error ->
                if (signed != null) {
                    eventFlow.value = AsyncEvent.Result(result = signed, error = null)
                } else {
                    eventFlow.value = errorEvent(error?.message ?: "Unknown error")
                }
            },
        )

        return eventFlow
    }
}
