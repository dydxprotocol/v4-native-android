package exchange.dydx.dydxstatemanager.clientState.transfers

import android.os.Build
import androidx.annotation.RequiresApi
import exchange.dydx.dydxstatemanager.clientState.DydxClientState
import exchange.dydx.trading.common.di.CoroutineScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface DydxTransferStateManagerProtocol {
    val state: StateFlow<DydxTransferState?>
    fun add(transfer: DydxTransferInstance)
    fun remove(transfer: DydxTransferInstance)
    fun clear()
}

@Singleton
class DydxTransferStateManager @Inject constructor(
    private val clientState: DydxClientState,
    @CoroutineScopes.App private val appScope: CoroutineScope,
) : DydxTransferStateManagerProtocol {
    companion object {
        private const val storeKey = "AbacusStateManager.TransferState"
        private val storeType: DydxClientState.StorageType = DydxClientState.StorageType.SharedPreferences
    }

    private val mutableState = MutableStateFlow<DydxTransferState?>(null)

    init {
        val state: DydxTransferState? = clientState.load(storeKey, storeType)
        mutableState.value = state

        appScope.launch {
            mutableState.collectLatest {
                clientState.store(it ?: DydxTransferState(), storeKey)
            }
        }
    }

    override val state: StateFlow<DydxTransferState?> = mutableState

    override fun add(transfer: DydxTransferInstance) {
        val current = mutableState.value ?: DydxTransferState()
        val isEmpty = current.transfers.isEmpty()
        if (isEmpty || !current.transfers.contains(transfer)) {
            mutableState.value = current.withUpdates { add(transfer) }
        }
    }

    override fun remove(transfer: DydxTransferInstance) {
        val current = mutableState.value
        val index = current?.transfers?.indexOfFirst { it == transfer }
        if (index != null && index != -1) {
            mutableState.value = current.withUpdates { removeAt(index) }
        }
    }

    override fun clear() {
        mutableState.value = DydxTransferState(emptyList())
    }
}

private fun DydxTransferState.withUpdates(block: MutableList<DydxTransferInstance>.() -> Unit): DydxTransferState {
    return copy(transfers = transfers.toMutableList().apply { block() }.toList())
}

@Serializable
data class DydxTransferState(
    val transfers: List<DydxTransferInstance> = emptyList(),
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
    val requestId: String? = null,
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
