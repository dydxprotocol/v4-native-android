package exchange.dydx.dydxstatemanager.clientState.walletmodal

import exchange.dydx.dydxstatemanager.clientState.DydxClientState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

interface DydxWalletModalStoreProtocol {
    val state: StateFlow<DydxWalletModal?>
    fun update(walletModal: DydxWalletModal)
    fun clear()
}

@Singleton
class DydxWalletModalStore @Inject constructor(
    private val clientState: DydxClientState,
) : DydxWalletModalStoreProtocol {
    companion object {
        private const val storeKey = "AbacusStateManager.WalletModalStore"
        private val storeType: DydxClientState.StorageType = DydxClientState.StorageType.SharedPreferences
    }

    private val mutableState = MutableStateFlow<DydxWalletModal?>(null)

    init {
        val state: DydxWalletModal? = clientState.load(storeKey, storeType)
        mutableState.value = state ?: DydxWalletModal.default
    }

    override val state: StateFlow<DydxWalletModal?> = mutableState

    override fun update(walletModal: DydxWalletModal) {
        mutableState.value = walletModal
        clientState.store(walletModal, storeKey)
    }
    override fun clear() {
        val current = DydxWalletModal(walletIds = emptyList())
        mutableState.value = current
        clientState.store(current, storeKey)
    }
}

@Serializable
data class DydxWalletModal(
    val walletIds: List<String> = listOf(),
) {
    companion object {
        val default = DydxWalletModal(
            walletIds = listOf(
                "c57ca95b47569778a828d19178114f4db188b89b763c899ba0be274e97267d96",
                "4622a2b2d6af1c9844944291e5e7351a6aa24cd7b23099efac1b2fd875da31a0",
                "971e689d0a5be527bac79629b4ee9b925e82208e5168b733496a09c0faed0709",
                "c03dfee351b6fcc421b4494ea33b9d4b92a984f87aa76d1663bb28705e95034a",
                "1ae92b26df02f0abca6304df07debccd18262fdf5fe82daa81593582dac9a369",
                "ecc4036f814562b41a5268adc86270fba1365471402006302e70169465b7ac18",
                "c286eebc742a537cd1d6818363e9dc53b21759a1e8e5d9b263d0c03ec7703576",
                "38f5d18bd8522c244bdd70cb4a68e0e718865155811c043f052fb9f1c51de662",
            ),
        )
    }
}
