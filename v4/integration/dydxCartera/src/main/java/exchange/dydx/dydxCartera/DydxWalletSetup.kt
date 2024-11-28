package exchange.dydx.dydxCartera

import android.content.Context
import exchange.dydx.cartera.CarteraConfig
import exchange.dydx.cartera.CarteraErrorCode
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.cartera.entities.Wallet
import exchange.dydx.cartera.walletprovider.WalletConnectCompletion
import exchange.dydx.cartera.walletprovider.WalletError
import exchange.dydx.cartera.walletprovider.WalletInfo
import exchange.dydx.cartera.walletprovider.WalletRequest
import exchange.dydx.cartera.walletprovider.WalletStatusDelegate
import exchange.dydx.cartera.walletprovider.WalletStatusProtocol
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

private const val TAG = "DydxWalletSetup"

open class DydxWalletSetup(
    open val context: Context,
    open val logger: Logging,
) : WalletStatusDelegate {

    private val walletConnectModalId = "walletconnect_modal"

    data class SetupResult(
        val ethereumAddress: String,
        val walletId: String?,
        val cosmosAddress: String?,
        val mnemonic: String?,
        val apiKey: String?,
        val secret: String?,
        val passPhrase: String?,
    )

    sealed class Status {
        object Idle : Status()
        object Started : Status()
        object Connected : Status()
        data class Signed(val setupResult: SetupResult) : Status()
        data class InProgress(val showSwitchWalletName: String?) : Status()
        data class Error(val error: WalletError) : Status()

        companion object {
            fun createError(error: WalletError): Error {
                return Error(error = error)
            }

            fun createError(message: String): Error {
                return Error(
                    error = WalletError(
                        code = CarteraErrorCode.UNEXPECTED_RESPONSE,
                        message = message,
                    ),
                )
            }
        }
    }

    open val _status: MutableStateFlow<Status> = MutableStateFlow(Status.Idle)
    val status: Flow<Status> = _status

    private val _debugLink: MutableStateFlow<String?> = MutableStateFlow(null)
    val debugLink: Flow<String?> = _debugLink

    val provider: CarteraProvider by lazy {
        val provider = CarteraProvider(context)
        provider.walletStatusDelegate = this
        provider
    }

    fun startDebugLink(chainId: String, completion: WalletConnectCompletion) {
        provider.disconnect()
        provider.startDebugLink(chainId, completion)
    }

    fun start(walletId: String?, ethereumChainId: Int, signTypedDataAction: String, signTypedDataDomainName: String) {
        val wallet: Wallet?
        val useWcModal: Boolean
        if (walletConnectModalId == walletId) {
            wallet = null
            useWcModal = true
        } else {
            wallet = CarteraConfig.shared?.wallets?.firstOrNull { it.id == walletId }
            if (wallet == null) {
                logger.e(TAG, "Wallet not found: $walletId")
            }
            useWcModal = false
        }

        _status.value = Status.Started
        val request = WalletRequest(
            wallet = wallet,
            address = null,
            chainId = ethereumChainId.toString(),
            context = context,
            useModal = useWcModal,
        )
        provider.connect(request) { info, error ->
            if (info?.address != null && error == null) {
                _status.value = Status.Connected
//                val walletName = info.wallet?.name ?: ""
//                Tracking.shared?.log(
//                    event = "ConnectWallet",
//                    data = mapOf("selectedWalletType" to walletName.uppercase(), "autoReconnect" to true)
//                )
                val signRequest = WalletRequest(
                    wallet = wallet,
                    address = info.address,
                    chainId = ethereumChainId.toString(),
                    context = context,
                    useModal = useWcModal,
                )
                sign(signRequest, info, signTypedDataAction, signTypedDataDomainName)
            } else if (error != null) {
                _status.value = Status.Error(error)
                provider.disconnect()
            }
        }
    }

    fun stop() {
        provider.disconnect()
        _status.value = Status.Idle
    }

    open fun sign(request: WalletRequest, connectedWallet: WalletInfo, signTypedDataAction: String, signTypedDataDomainName: String) {
        // Implementation for the sign() method
    }

    override fun statusChanged(status: WalletStatusProtocol) {
        _debugLink.value = status.connectionDeeplink
    }
}
