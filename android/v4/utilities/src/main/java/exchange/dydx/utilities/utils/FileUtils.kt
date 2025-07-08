package exchange.dydx.utilities.utils

import android.content.Context
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

private const val TAG = "FileUtils"

class FileUtils @Inject constructor(
    private val logger: Logging
) {
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
            logger.e(TAG, "error: $e")
            e.printStackTrace()
            null
        }
    }

    fun compressFile(context: Context, sourceFilePath: String, compressedFilePath: String): Boolean {
        val bufferSize = 1024
        try {
            val sourceFile = File(sourceFilePath)
            val compressedFile = File(compressedFilePath)

            val fileInputStream = FileInputStream(sourceFile)
            val zipOutputStream = ZipOutputStream(BufferedOutputStream(FileOutputStream(compressedFile)))
            val zipEntry = ZipEntry(sourceFile.name)
            zipOutputStream.putNextEntry(zipEntry)

            val buffer = ByteArray(bufferSize)
            var length: Int
            while (fileInputStream.read(buffer).also { length = it } > 0) {
                zipOutputStream.write(buffer, 0, length)
            }

            zipOutputStream.closeEntry()
            zipOutputStream.close()
            fileInputStream.close()
            return true
        } catch (e: IOException) {
            logger.e(TAG, "Error compressing file: $e")
            e.printStackTrace()
            return false
        }
    }
}
