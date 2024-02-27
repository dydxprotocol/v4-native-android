package exchange.dydx.utilities.utils

import android.content.Context
import android.util.Log

object FileUtils {
    fun loadFromAssets(context: Context, fileName: String): String? {
        return try {
            val manager = context.assets
            val inputStream = manager.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer)
        } catch (e: Exception) {
            Log.e("FileUtils", "error: $e")
            null
        }
    }
}
