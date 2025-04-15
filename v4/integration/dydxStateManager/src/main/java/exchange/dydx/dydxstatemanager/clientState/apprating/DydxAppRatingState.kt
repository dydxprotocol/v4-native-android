package exchange.dydx.dydxstatemanager.clientState.apprating

import exchange.dydx.dydxstatemanager.clientState.DydxClientState
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

interface DydxAppRatingStateManagerProtocol {
    val state: StateFlow<DydxAppRatingState?>

    fun update(state: DydxAppRatingState)
    fun reset()
}

private val TAG = "DydxAppRatingStateManager"

@Singleton
class DydxAppRatingStateManager @Inject constructor(
    private val clientState: DydxClientState,
    private val logger: Logging,
) : DydxAppRatingStateManagerProtocol {

    companion object {
        private const val storeKey = "AbacusStateManager.AppRating"
        private val storeType: DydxClientState.StorageType = DydxClientState.StorageType.SharedPreferences
    }

    private val mutableState = MutableStateFlow<DydxAppRatingState?>(null)

    init {
        try {
            val state: DydxAppRatingState? = clientState.load(storeKey, storeType)
            mutableState.value = state ?: DydxAppRatingState.default()
        } catch (e: Exception) {
            logger.e(TAG, "Failed to load AppRatingState, resetting to default: " + e.message)
            mutableState.value = DydxAppRatingState.default()
        }
    }

    override val state: StateFlow<DydxAppRatingState?> = mutableState

    override fun update(state: DydxAppRatingState) {
        mutableState.value = state
        clientState.store(state, storeKey, storeType)
    }

    override fun reset() {
        mutableState.value = DydxAppRatingState.default()
        clientState.reset(storeKey, storeType)
    }
}

@Serializable
data class DydxAppRatingState(
    val transfersCreatedSinceLastPrompt: Set<String>,
    val ordersCreatedSinceLastPrompt: Set<String>,
    val uniqueDayAppOpensCount: Int,
    val lastAppOpenTimestamp: Double,
    val lastPromptedTimestamp: Double,
    val hasEverConnectedWallet: Boolean,
    val shouldStopPreprompting: Boolean
) {
    companion object {
        fun default(): DydxAppRatingState {
            val currentTime = System.currentTimeMillis().toDouble()
            return DydxAppRatingState(
                transfersCreatedSinceLastPrompt = emptySet(),
                ordersCreatedSinceLastPrompt = emptySet(),
                uniqueDayAppOpensCount = 0,
                lastAppOpenTimestamp = currentTime,
                lastPromptedTimestamp = currentTime,
                hasEverConnectedWallet = false,
                shouldStopPreprompting = false,
            )
        }
    }
}
