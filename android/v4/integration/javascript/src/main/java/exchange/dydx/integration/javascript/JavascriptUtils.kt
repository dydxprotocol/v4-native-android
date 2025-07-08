package exchange.dydx.integration.javascript

import android.content.Context
import java.io.InputStream

object JavascriptUtils {
    fun loadAsset(context: Context, fileName: String?): String? {
        if (fileName != null) {
            var text: String? = null
            try {
                val inputStream: InputStream = context.assets.open(fileName)
                text = inputStream.bufferedReader().use { it.readText() }
            } catch (ex: Exception) {
                ex.printStackTrace()
                return null
            }
            return text
        }
        return null
    }
}
