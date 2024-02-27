package exchange.dydx.utilities.utils

import android.content.Context
import android.util.Log
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

object JsonUtils {
    inline fun <reified T> loadFromAssets(context: Context, fileName: String): T? {
        return try {
            val manager = context.assets
            val inputStream = manager.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val data = String(buffer)
            Json { ignoreUnknownKeys = true }.decodeFromString<T>(data)
        } catch (e: Exception) {
            Log.e("JsonUtils", "error: $e")
            null
        }
    }
}

fun String.jsonStringToMap(): Map<String, Any>? {
    try {
        val json = Json.parseToJsonElement(this)
        return json.jsonObject.toMap()
    } catch (e: Exception) {
        Log.e("JsonUtils", "error: $e")
        return null
    }
}
