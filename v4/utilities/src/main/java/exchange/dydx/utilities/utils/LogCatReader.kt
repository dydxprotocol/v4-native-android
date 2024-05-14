package exchange.dydx.utilities.utils

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import javax.inject.Inject

private const val TAG = "LogCatReader"

class LogCatReader @Inject constructor(
    private val logging: Logging
) {
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
            logging.e(TAG, "saveLogCatToFile: $e")
            return false
        }
    }
}
