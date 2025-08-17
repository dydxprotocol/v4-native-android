package exchange.dydx.dydxstatemanager.clientState.wallets

import exchange.dydx.dydxCartera.CarteraConfig
import exchange.dydx.dydxCartera.imageUrl
import exchange.dydx.dydxstatemanager.clientState.DydxClientState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

interface DydxWalletStateManagerProtocol {
    val state: StateFlow<DydxWalletState?>
    fun setCurrentWallet(wallet: DydxWalletInstance)
    fun clear()
    fun replaceWallet()
}

@Singleton
class DydxWalletStateManager @Inject constructor(
    private val clientState: DydxClientState,
) : DydxWalletStateManagerProtocol {
    companion object {
        private const val storeKey = "AbacusStateManager.WalletState"
        private val storeType: DydxClientState.StorageType = DydxClientState.StorageType.SecurePreferences
    }

    private val mutableState = MutableStateFlow<DydxWalletState?>(null)

    init {
        val state: DydxWalletState? = clientState.load(storeKey, storeType)
        mutableState.value = state
    }

    override val state: StateFlow<DydxWalletState?> = mutableState

    override fun setCurrentWallet(wallet: DydxWalletInstance) {
        var mutableWallets = mutableState.value?.wallets?.toMutableList() ?: mutableListOf()
        var index = mutableWallets.indexOfFirst { it.walletId == wallet.walletId && it.ethereumAddress == wallet.ethereumAddress }
        if (index != -1) {
            val existingWallet = mutableWallets[index]
            if (wallet != existingWallet) {
                existingWallet.mergeWith(wallet)
                mutableWallets[index] = existingWallet
            }
        } else {
            mutableWallets = (listOf(wallet) + mutableWallets).toMutableList()
            index = 0
        }
        val newState = DydxWalletState(mutableWallets, null, index)
        mutableState.value = newState
        clientState.store(newState, storeKey, storeType)
    }

    override fun clear() {
        val newState = DydxWalletState(emptyList(), null, null)
        mutableState.value = newState
        clientState.reset(storeKey, storeType)
    }

    override fun replaceWallet() {
        var mutableWallets = mutableState.value?.wallets?.toMutableList() ?: mutableListOf()
        var indexToCurrent = mutableState.value?.indexToCurrent
        if (indexToCurrent != null && indexToCurrent < mutableWallets.size) {
            mutableWallets.removeAt(indexToCurrent)
        }
        if (mutableWallets.isEmpty()) {
            val newState = DydxWalletState(emptyList(), null, null)
            mutableState.value = newState
            clientState.reset(storeKey, storeType)
        } else {
            val newState = DydxWalletState(mutableWallets, null, 0)
            mutableState.value = newState
            clientState.store(newState, storeKey, storeType)
        }
    }
}

@Serializable
data class DydxWalletState(
    var wallets: List<DydxWalletInstance> = listOf(),
    var keyToCurrent: String? = null, // Deprecated
    val indexToCurrent: Int? = null,
) {
    val currentWallet: DydxWalletInstance?
        get() = if (indexToCurrent != null) wallets.get(indexToCurrent) else null
}

@Serializable
data class DydxWalletInstance(
    var ethereumAddress: String? = null,
    var walletId: String? = null,
    var cosmoAddress: String? = null,
    var mnemonic: String? = null,
    var subaccountNumber: String? = null,
    var apiKey: String? = null,
    var secret: String? = null,
    var passPhrase: String? = null,
    var svmAddress: String? = null,
    var avalancheAddress: String? = null,
    var sourceWalletMnemonic: String? = null,
    var loginMethod: String? = null,
    var userEmail: String? = null,
) {
    companion object {
        fun v4(
            ethereumAddress: String?,
            walletId: String?,
            cosmoAddress: String,
            dydxMnemonic: String,
            svmAddress: String?,
            avalancheAddress: String?,
            sourceWalletMnemonic: String?,
            loginMethod: String?,
            userEmail: String?,
        ): DydxWalletInstance {
            return DydxWalletInstance(
                ethereumAddress = ethereumAddress,
                walletId = walletId,
                cosmoAddress = cosmoAddress,
                mnemonic = dydxMnemonic,
                svmAddress = svmAddress,
                avalancheAddress = avalancheAddress,
                sourceWalletMnemonic = sourceWalletMnemonic,
                loginMethod = loginMethod,
                userEmail = userEmail,
            )
        }
    }

    fun mergeWith(another: DydxWalletInstance) {
        if (walletId == another.walletId) {
            ethereumAddress = another.ethereumAddress
            cosmoAddress?.let { cosmoAddress = another.cosmoAddress }
            mnemonic?.let { mnemonic = another.mnemonic }
            subaccountNumber?.let { subaccountNumber = another.subaccountNumber }
            apiKey?.let { apiKey = another.apiKey }
            secret?.let { secret = another.secret }
            passPhrase?.let { passPhrase = another.passPhrase }
            svmAddress?.let { svmAddress = another.svmAddress }
            avalancheAddress?.let { avalancheAddress = another.avalancheAddress }
            sourceWalletMnemonic?.let { sourceWalletMnemonic = another.sourceWalletMnemonic }
            loginMethod?.let { loginMethod = another.loginMethod }
            userEmail?.let { userEmail = another.userEmail }
        }
    }

    fun imageUrl(folder: String): String? {
        if (walletId != null) {
            val carteraWallet = CarteraConfig.shared?.wallets?.firstOrNull { wallet ->
                wallet.id == walletId
            }
            return carteraWallet?.imageUrl(folder)
        }
        return null
    }
}
