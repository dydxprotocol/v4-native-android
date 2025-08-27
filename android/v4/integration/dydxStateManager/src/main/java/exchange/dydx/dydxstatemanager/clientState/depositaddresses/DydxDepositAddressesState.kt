package exchange.dydx.dydxstatemanager.clientState.depositaddresses

import exchange.dydx.dydxstatemanager.clientState.DydxClientState
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

interface DydxDepositAddressesStateManagerProtocol {
    val state: StateFlow<DepositAddresses?>

    fun update(state: DepositAddresses)
    fun reset()
}

private val TAG = "DydxDepositAddressesStateManager"

@Singleton
class DydxDepositAddressesStateManager @Inject constructor(
    private val clientState: DydxClientState,
    private val logger: Logging,
) : DydxDepositAddressesStateManagerProtocol {

    companion object {
        private const val storeKey = "AbacusStateManager.DepositAddresses"
        private val storeType: DydxClientState.StorageType = DydxClientState.StorageType.SharedPreferences
    }

    private val mutableState = MutableStateFlow<DepositAddresses?>(null)

    init {
        try {
            val state: DepositAddresses? = clientState.load(
                storeKey,
                storeType,
            )
            mutableState.value = state ?: DepositAddresses()
        } catch (e: Exception) {
            logger.e(TAG, "Failed to load DepositAddresses, resetting to default: " + e.message)
            mutableState.value = DepositAddresses()
        }
    }

    override val state: StateFlow<DepositAddresses?> = mutableState

    override fun update(state: DepositAddresses) {
        mutableState.value = state
        clientState.store(
            state,
            storeKey,
            storeType,
        )
    }

    override fun reset() {
        mutableState.value = DepositAddresses()
        clientState.reset(
            storeKey,
            storeType,
        )
    }
}

@Serializable
data class DepositAddresses(
    val evmAddress: String? = null,
    val avalancheAddress: String? = null,
    val svmAddress: String? = null
)
