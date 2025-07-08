package exchange.dydx.utilities.utils

import android.content.Context
import android.util.Log
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

object JsonUtils {
    val json = Json { ignoreUnknownKeys = true }

    inline fun <reified T> loadFromAssets(context: Context, fileName: String): T? {
        return try {
            val manager = context.assets
            val inputStream = manager.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val data = String(buffer)
            json.decodeFromString<T>(data)
        } catch (e: Exception) {
            Log.e("JsonUtils", "error: $e")
            null
        }
    }
}

fun String.jsonStringToMap(): Map<String, Any>? {
    try {
        val json = Json.parseToJsonElement(this)
        return json.jsonObject
    } catch (e: SerializationException) {
        Log.e("JsonUtils", "error: $e")
        return null
    }
}

/**
 * JsonLiterals wrap Strings in quotations, so if we're passing generically to code we don't own (Amplitude),
 * we'll get extra quotations. Therefore, need to replace any JsonLiterals with isString = true with the
 * actual content strings.
 */
fun String.jsonStringToRawStringMap(): Map<String, Any>? {
    try {
        val json = Json.parseToJsonElement(this)
        return json.jsonObject.toRawStringMap()
    } catch (e: SerializationException) {
        Log.e("JsonUtils", "error: $e")
        return null
    }
}

private fun JsonObject.toRawStringMap(): Map<String, Any> {
    return this.mapValues { (_, value) ->
        when (value) {
            is JsonPrimitive -> if (value.isString) value.content else value
            is JsonObject -> value.toRawStringMap()
            is JsonArray -> value.map { it.toRawString() }
            else -> value.toString()
        }
    }
}

private fun JsonElement.toRawString(): Any {
    return when (this) {
        is JsonPrimitive -> if (this.isString) this.content else this
        is JsonObject -> this.toRawStringMap()
        is JsonArray -> this.map { it.toRawString() }
        else -> this.toString()
    }
}
