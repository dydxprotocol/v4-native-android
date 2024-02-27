package exchange.dydx.utilities.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File
import java.net.URL

class CachedFileLoader(
    private val context: Context,
) {
    fun loadString(filePath: String, url: String?, completion: (String?) -> Unit) {
        loadData(filePath, url) { data ->
            data?.let { byteArray ->
                completion(String(byteArray))
            }
        }
    }

    fun loadData(filePath: String, url: String?, completion: (ByteArray?) -> Unit) {
        val cachedFile = cachedFilePath(filePath)
        if (cachedFile?.exists() == true) {
            completion(cachedFile.readBytes())
        } else {
            FileUtils.loadFromAssets(context, filePath)?.let { string ->
                completion(string.toByteArray())
            }
        }

        if (url != null) {
            Thread {
                try {
                    val data = URL(url).readBytes()
                    if (cachedFile?.exists() == true) {
                        cachedFile.delete()
                    }
                    // Create parent directories if they don't exist
                    val parentDir = cachedFile?.parentFile
                    if (parentDir != null) {
                        if (!parentDir.exists()) {
                            parentDir.mkdirs()
                        }
                    }
                    cachedFile?.writeBytes(data)
                    Handler(Looper.getMainLooper()).post {
                        completion(data)
                    }
                } catch (e: Exception) {
                    Log.e("CachedFileLoader", "error: $e")
                    Handler(Looper.getMainLooper()).post {
                        completion(null)
                    }
                }
            }.start()
        }
    }

    private fun cachedFilePath(filePath: String): File? {
        return File(context.filesDir, filePath)
    }
}
