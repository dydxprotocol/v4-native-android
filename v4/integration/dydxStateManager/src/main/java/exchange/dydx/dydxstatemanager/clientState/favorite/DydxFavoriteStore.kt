package exchange.dydx.dydxstatemanager.clientState.favorite

import exchange.dydx.dydxstatemanager.clientState.DydxClientState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

interface DydxFavoriteStoreProtocol {
    val state: Flow<DydxUserFavorite?>

    fun isFavorite(marketId: String): Boolean
    fun setFavorite(isFavorite: Boolean, marketId: String)
    fun clear()
}

@Singleton
class DydxFavoriteStore @Inject constructor(
    private val clientState: DydxClientState,
) : DydxFavoriteStoreProtocol {

    companion object {
        private const val storeKey = "AbacusStateManager.FavoriteStore"
        private val storeType: DydxClientState.StorageType = DydxClientState.StorageType.SharedPreferences
    }

    private val mutableState = MutableStateFlow<DydxUserFavorite?>(null)

    init {
        val state: DydxUserFavorite? = clientState.load(storeKey, storeType)
        mutableState.value = state
    }

    override val state: Flow<DydxUserFavorite?> = mutableState

    override fun isFavorite(marketId: String): Boolean {
        return mutableState.value?.marketIds?.contains(marketId) ?: false
    }

    override fun setFavorite(isFavorite: Boolean, marketId: String) {
        val current = mutableState.value ?: DydxUserFavorite()
        if (isFavorite) {
            val isEmpty = current.marketIds.isNullOrEmpty()
            if (isEmpty || !current.marketIds.contains(marketId)) {
                val marketIds = current.marketIds.toMutableList()
                marketIds.add(marketId)
                val newState = DydxUserFavorite(marketIds = marketIds)
                mutableState.value = newState
                clientState.store(newState, storeKey)
            }
        } else {
            val index = current.marketIds.indexOfFirst { it == marketId }
            if (index != null && index != -1) {
                val marketIds = current.marketIds.toMutableList()
                marketIds.removeAt(index)
                val newState = DydxUserFavorite(marketIds = marketIds)
                mutableState.value = newState
                clientState.store(newState, storeKey)
            }
        }
    }

    override fun clear() {
        val current = DydxUserFavorite(marketIds = emptyList())
        mutableState.value = current
        clientState.store(current, storeKey)
    }
}

@Serializable
data class DydxUserFavorite(
    val marketIds: List<String> = listOf(),
)
