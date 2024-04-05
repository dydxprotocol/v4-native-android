package exchange.dydx.dydxstatemanager.clientState

import exchange.dydx.utilities.utils.SecureStore
import exchange.dydx.utilities.utils.SharedPreferencesStore
import exchange.dydx.utilities.utils.StoreProtocol
import exchange.dydx.utilities.utils.read
import exchange.dydx.utilities.utils.save
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DydxClientState @Inject constructor(
    private val sharedPreferencesStore: SharedPreferencesStore,
    private val secureStore: SecureStore,
) {
    val TAG = "dydxClientState"

    enum class StorageType {
        SharedPreferences,
        SecurePreferences,
    }

    inline fun <reified T : Any> load(storeKey: String, storeType: StorageType = StorageType.SharedPreferences): T? {
        val store = getStore(storeType)
        return store.read<T>(storeKey)
    }

    inline fun <reified T : Any> store(state: T, storeKey: String, storeType: StorageType = StorageType.SharedPreferences) {
        val store = getStore(storeType)
        store.save(state, storeKey)
    }

    fun reset(storeKey: String, storeType: StorageType = StorageType.SharedPreferences) {
        val store = getStore(storeType)
        store.delete(storeKey)
    }

    fun getStore(storeType: StorageType): StoreProtocol {
        return when (storeType) {
            StorageType.SharedPreferences -> sharedPreferencesStore
            StorageType.SecurePreferences -> secureStore
        }
    }
}
