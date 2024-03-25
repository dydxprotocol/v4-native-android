package exchange.dydx.utilities.utils

import android.content.Context
import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object FileUtils {

    private const val TAG = "FileUtils"

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
            Log.e(TAG, "error: $e")
            e.printStackTrace()
            null
        }
    }

    fun compressFile(context: Context, sourceFilePath: String, compressedFilePath: String) {
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
        } catch (e: IOException) {
            Log.e(TAG, "Error compressing file: $e")
            e.printStackTrace()
        }
    }
}
