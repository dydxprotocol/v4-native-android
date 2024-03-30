package exchange.dydx.utilities.utils

import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

object LogCatReader {

    private const val TAG = "LogCatReader"

    /**
     * Reads the logcat and stores the log in a file.
     */
    fun saveLogCatToFile(logFile: File): Boolean {
        try {
            val process = Runtime.getRuntime().exec("logcat -d")
            val reader = process.inputStream.bufferedReader()
            val writer = BufferedWriter(FileWriter(logFile))

            var line: String? = reader.readLine()
            while (line != null) {
                writer.write(line)
                writer.newLine()
                line = reader.readLine()
            }

            writer.close()
            reader.close()
            process.destroy()
            return true
        } catch (e: IOException) {
            Log.e(TAG, "saveLogCatToFile: $e")
            return false
        }
    }
}
