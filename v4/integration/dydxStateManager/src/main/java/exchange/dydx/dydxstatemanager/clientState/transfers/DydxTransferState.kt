package exchange.dydx.dydxstatemanager.clientState.transfers

import android.os.Build
import androidx.annotation.RequiresApi
import exchange.dydx.dydxstatemanager.clientState.DydxClientState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface DydxTransferStateManagerProtocol {
    val state: Flow<DydxTransferState?>
    fun add(transfer: DydxTransferInstance)
    fun remove(transfer: DydxTransferInstance)
    fun clear()
}

@Singleton
class DydxTransferStateManager @Inject constructor(
    private val clientState: DydxClientState,
) : DydxTransferStateManagerProtocol {
    companion object {
        private const val storeKey = "AbacusStateManager.TransferState"
        private val storeType: DydxClientState.StorageType = DydxClientState.StorageType.SharedPreferences
    }

    private val mutableState = MutableStateFlow<DydxTransferState?>(null)

    init {
        val state: DydxTransferState? = clientState.load(storeKey, storeType)
        mutableState.value = state
    }

    override val state: Flow<DydxTransferState?> by lazy { mutableState }

    override fun add(transfer: DydxTransferInstance) {
        val current = mutableState.value ?: DydxTransferState()
        val isEmpty = current.transfers.isNullOrEmpty()
        if (isEmpty || !current.transfers.contains(transfer)) {
            val transfers = current.transfers.toMutableList()
            transfers.add(transfer)
            current.transfers = transfers
            clientState.store(current, storeKey)
        }
    }

    override fun remove(transfer: DydxTransferInstance) {
        val current = mutableState.value
        val index = current?.transfers?.indexOfFirst { it == transfer }
        if (index != null && index != -1) {
            val transfers = current.transfers.toMutableList()
            transfers.removeAt(index)
            current.transfers = transfers
            clientState.store(current, storeKey)
        }
    }

    override fun clear() {
        val current = mutableState.value ?: DydxTransferState()
        current.transfers = emptyList()
        clientState.store(current, storeKey)
    }
}

@Serializable
data class DydxTransferState(
    var transfers: List<DydxTransferInstance> = emptyList(),
)

@Serializable
data class DydxTransferInstance(
    val transferType: TransferType,
    val transactionHash: String,
    val fromChainId: String? = null,
    val fromChainName: String? = null,
    val toChainId: String? = null,
    val toChainName: String? = null,
    val dateIntoEpochMilli: Long? = null,
    val usdcSize: Double? = null,
    val size: Double? = null,
    val isCctp: Boolean? = null,
) {
    enum class TransferType {
        DEPOSIT, WITHDRAWAL, TRANSFER_OUT
    }

    val date: Instant?
        @RequiresApi(Build.VERSION_CODES.O)
        get() {
            return dateIntoEpochMilli?.let { Instant.ofEpochMilli(it) }
        }
}
