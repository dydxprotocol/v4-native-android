package exchange.dydx.integration.starkex

import android.content.Context
import exchange.dydx.integration.javascript.JavascriptApiImpl
import exchange.dydx.integration.javascript.JavascriptRunnerV3
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.IOException

class StarkexLib(
    context: Context,
    filename: String = "starkex-lib.js",
) :
    JavascriptApiImpl(
        context = context,
        description = filename,
        runner = JavascriptRunnerV3.runnerFromFile(context, filename)
            ?: throw IOException("Fatal, unable to load runner from: $filename"),
    ) {

    private val TAG = "StarkexLib"

    fun aesDecrypt(
        input: String,
        password: String,
        callback: ((String?, RuntimeException?) -> Unit),
    ) {
        Timber.tag(TAG).i("Running aesDecrypt on: %s", input)
        runBlocking {
            runner.runJs(
                "" +
                    "helper.aesDecrypt('$input','$password')",
            ) {
                val data = it?.response
                if (data != null && "null" != data) {
                    val startIndex = if (data.startsWith('\"')) 1 else 0
                    val endOffset = if (data.endsWith('\"')) 1 else 0
                    val preparedString = data
                        // strip out surrounding quotation marks
                        .substring(startIndex = startIndex, endIndex = data.length - endOffset)
                        // unescape quote literals
                        .replace("\\\"", "\"")

                    callback(preparedString, null)
                } else {
                    callback(null, java.lang.RuntimeException("Decrypt failed"))
                }
            }
        }
    }
}

// TODO move this into its own fine when it has some content, or delete if not used.
class StarkexEth(
    context: Context,
    filename: String = "starkex-eth.js",
) : JavascriptApiImpl(
    context = context,
    description = filename,
    runner = JavascriptRunnerV3.runnerFromFile(context, filename)
        ?: throw IOException("Fatal, unable to load runner from: $filename"),
)
