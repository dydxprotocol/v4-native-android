package exchange.dydx.utilities.utils

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader

object LogCatReader {

    private const val TAG = "LogCatReader"

    /**
     * Reads the logcat and returns the log as a string.
     */
    fun readLogCat(): String {
        val log = StringBuilder()
        try {
            val process = Runtime.getRuntime()
                .exec("logcat -d") // -d flag reads only the current contents of the log
            val bufferedReader = BufferedReader(
                InputStreamReader(process.inputStream),
            )
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                log.append(line).append("\n")
            }
            process.destroy()
        } catch (e: IOException) {
            Log.e(TAG, "Error reading logcat: $e")
        }
        return log.toString()
    }

    /**
     * Reads the logcat and stores the log in a file.
     */
    fun saveLogCatToFile(context: Context, filePath: String) {
        try {
            val process = Runtime.getRuntime().exec("logcat -d")
            val reader = process.inputStream.bufferedReader()

            val logFile = File(context.getExternalFilesDir(null), filePath)
            val writer = BufferedWriter(FileWriter(logFile))

            var line: String? = reader.readLine()
            while (line != null) {
                writer.write(line)
                writer.newLine()
                line = reader.readLine()
            }

            writer.close()
            reader.close()
        } catch (e: IOException) {
            Log.e(TAG, "saveLogCatToFile: $e")
        }
    }
}
