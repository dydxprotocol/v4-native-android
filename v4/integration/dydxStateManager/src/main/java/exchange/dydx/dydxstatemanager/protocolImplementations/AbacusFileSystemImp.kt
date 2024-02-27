package exchange.dydx.dydxstatemanager.protocolImplementations

import android.content.Context
import android.util.Log
import exchange.dydx.abacus.protocols.FileLocation
import exchange.dydx.abacus.protocols.FileSystemProtocol
import okio.use
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class AbacusFileSystemImp(
    private val context: Context?,
) : FileSystemProtocol {
    private val TAG = "AbacusFileSystemImp"

    override fun readTextFile(location: FileLocation, path: String): String? {
        val path = if (path.first() == '/') path.drop(1) else path
        when (location) {
            FileLocation.AppBundle -> {
                val context = context
                return if (context != null) {
                    try {
                        val string = context.assets.open(path).bufferedReader().use {
                            it.readText()
                        }
                        string
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e.stackTraceToString())
                        null
                    }
                } else {
                    Timber.tag(TAG).e("Context is null")
                    null
                }
            }
            FileLocation.AppDocs -> {
                val context = context
                return if (context != null) {
                    val absoluteFilePath = File(context.filesDir, path).absolutePath
                    val file = File(absoluteFilePath)
                    if (file.exists()) {
                        file.readText()
                    } else {
                        Timber.tag(TAG).e("File does not exist: %s", absoluteFilePath)
                        null
                    }
                } else {
                    Timber.tag(TAG).e("Context is null")
                    null
                }
            }
        }
    }

    override fun writeTextFile(path: String, text: String): Boolean {
        val context = context
        if (context != null) {
            val path = if (path.first() == '/') path.drop(1) else path
            val absoluteFilePath = File(context.filesDir, path).absolutePath

            // Create parent directories if they don't exist
            val parentDir = File(absoluteFilePath).parentFile
            if (parentDir != null) {
                if (!parentDir.exists()) {
                    parentDir.mkdirs()
                }
            }

            try {
                val fos = FileOutputStream(absoluteFilePath)
                fos.write(text.toByteArray())
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        } else {
            Log.e(TAG, "Context is null")
            return false
        }
    }
}
