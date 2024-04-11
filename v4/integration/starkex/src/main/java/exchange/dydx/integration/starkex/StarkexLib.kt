package exchange.dydx.integration.starkex

import android.app.Application
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.integration.javascript.JavascriptApiImpl
import exchange.dydx.integration.javascript.JavascriptRunnerV3
import exchange.dydx.trading.common.di.CoroutineScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

private const val STARKEX_FILENAME: String = "starkex-lib.js"

@ActivityRetainedScoped
class StarkexLib @Inject constructor(
    application: Application,
    @CoroutineScopes.App appScope: CoroutineScope,
) :
    JavascriptApiImpl(
        context = application,
        description = STARKEX_FILENAME,
        runner = JavascriptRunnerV3.runnerFromFile(appScope, application, STARKEX_FILENAME)
            ?: throw IOException("Fatal, unable to load runner from: $STARKEX_FILENAME"),
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

private const val STARKEX_ETH_FILENAME: String = "starkex-eth.js"

// TODO move this into its own fine when it has some content, or delete if not used.
@ActivityRetainedScoped
class StarkexEth @Inject constructor(
    application: Application,
    @CoroutineScopes.App appScope: CoroutineScope,
) : JavascriptApiImpl(
    context = application,
    description = STARKEX_ETH_FILENAME,
    runner = JavascriptRunnerV3.runnerFromFile(appScope, application, STARKEX_ETH_FILENAME)
        ?: throw IOException("Fatal, unable to load runner from: $STARKEX_ETH_FILENAME"),
)
