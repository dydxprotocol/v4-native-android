package exchange.dydx.utilities.utils

import android.util.Log
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface StoreProtocol {
    val stateUpdatedCount: StateFlow<Int>

    fun save(data: String, key: String)

    fun read(key: String): String?

    fun read(key: String, defaultValue: String): String

    fun delete(key: String)
}

inline fun <reified T> StoreProtocol.save(obj: T, key: String) {
    val jsonString = Json { ignoreUnknownKeys = true }.encodeToString(obj)
    save(jsonString, key)
}

inline fun <reified T> StoreProtocol.read(key: String): T? {
    val TAG = "StoreProtocol"
    val jsonString = read(key)
    return if (!jsonString.isNullOrEmpty()) {
        Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
    } else {
        Log.d(TAG, "read: jsonString is null")
        null
    }
}
