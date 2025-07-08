package exchange.dydx.dydxstatemanager.protocolImplementations

import android.app.Application
import exchange.dydx.abacus.protocols.FileLocation
import exchange.dydx.abacus.protocols.FileSystemProtocol
import exchange.dydx.utilities.utils.Logging
import okio.use
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class AbacusFileSystemImp @Inject constructor(
    private val application: Application,
    private val logger: Logging,
) : FileSystemProtocol {
    private val TAG = "AbacusFileSystemImp"

    override fun readTextFile(location: FileLocation, path: String): String? {
        val path = if (path.first() == '/') path.drop(1) else path
        when (location) {
            FileLocation.AppBundle -> {
                return try {
                    val string = application.assets.open(path).bufferedReader().use {
                        it.readText()
                    }
                    string
                } catch (e: Exception) {
                    logger.e(TAG, "Failed to read file: $path, error: ${e.stackTraceToString()}")
                    null
                }
            }
            FileLocation.AppDocs -> {
                val absoluteFilePath = File(application.filesDir, path).absolutePath
                val file = File(absoluteFilePath)

                return if (file.exists()) {
                    file.readText()
                } else {
                    logger.e(TAG, "File does not exist: $absoluteFilePath")
                    null
                }
            }
        }
    }

    override fun writeTextFile(path: String, text: String): Boolean {
        val path = if (path.first() == '/') path.drop(1) else path
        val absoluteFilePath = File(application.filesDir, path).absolutePath

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
    }
}
