package exchange.dydx.integration.javascript

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import exchange.dydx.trading.common.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.regex.Pattern

private const val TAG: String = "JavascriptRunner(V4)"
private const val DO_LOG: Boolean = AppConfig.VERBOSE_LOGGING
val LOGGER = if (DO_LOG) Timber.tag(TAG) else null

class JavascriptRunnerV4 constructor(
    private val scriptDescription: String,
    private val scriptInitializationCode: String,
    private val scope: CoroutineScope,
) : JavascriptRunner {

    override val initialized = MutableStateFlow(false)

    var callBackMap: MutableMap<String, ResultCallback> = mutableMapOf()

    companion object {
        fun runnerFromFile(
            scope: CoroutineScope,
            context: Context,
            file: String,
        ): JavascriptRunnerV4? {
            val script = JavascriptUtils.loadAsset(context, file)
            if (script != null) {
                return JavascriptRunnerV4(file, script, scope)
            }
            return null
        }
    }

    @JavascriptInterface
    fun onJsAsyncResult(key: String, result: String?) {
        val callback = callBackMap[key]
        if (callback == null) {
            LOGGER?.e("No callback found for key: $key")
            return
        }
        callBackMap.remove(key)
        callback.invoke(JavascriptRunnerResult(response = result))
    }

    override var webview: WebView? = null
        set(value) {
            LOGGER?.d("Setting Webview")
            if (field == value) {
                LOGGER?.d("Noop update")
                return
            }
            if (field != null) {
                Timber.tag(TAG).w("Updating webview in runner")
            }
            initialized.value = false
            if (value != null) {
//                value.settings.javaScriptEnabled = true
                LOGGER?.d("Loading base url")
            }

            value?.addJavascriptInterface(this, "bridge")
            field = value
        }

    override fun webviewClient(): WebViewClient {
        return object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                Timber.tag(TAG).w("Error in webview client: %s", error)
            }
            override fun onPageFinished(view: WebView, weburl: String) {
                initializeJavascriptEnvironment() {
                    LOGGER?.i("Initialized javascript environment: $it")
                    initialized.value = true
                }
            }
        }
    }

    override val webappInterface: WebAppInterface = WebAppInterface()

    override suspend fun runJs(function: String, params: List<String>, callback: ResultCallback) {
        val tranformedParams: MutableList<String> = params.toMutableList()
        val paramsText = tranformedParams.joinToString(",")

        val key = "key" + System.currentTimeMillis().toString()
        callBackMap[key] = callback

        val script = """
            function bridgeFunction() {
               $function($paramsText).then(function(result) {
                  bridge.onJsAsyncResult("$key", result);
               });
            };

            bridgeFunction();
        """.trimIndent()

        runJs(script) { }
    }

    override suspend fun runJs(script: String, callback: ResultCallback) {
        launchJs(script, callback)
    }

    fun initializeJavascriptEnvironment(callback: ResultCallback) {
        launchJs(scriptInitializationCode, callback)
    }

    val pattern = Pattern.compile("""^"(.*)"\$""")
    private fun launchJs(script: String, callback: ResultCallback) {
        scope.launch {
            webappInterface.callback = callback
            val localWebview = webview
            if (localWebview == null) {
                Timber.e("Unable to run function, no webview present, $script")
                return@launch
            }
            try {
                LOGGER?.i("Running script: ${script.take(1024)}")
                localWebview.evaluateJavascript(
                    script,
                ) {
                    LOGGER?.i("Script completed: $it")
                    val result = it.removeSurrounding("\"")
                    if (result != it) {
                        LOGGER?.d("Stripped surrounding quotes")
                    }
                    callback.invoke(
                        JavascriptRunnerResult(
                            response =
                            if ("null".equals(result, ignoreCase = true)) {
                                null
                            } else {
                                result
                            },
                        ),
                    )
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error executing script: $script")
            }
        }
    }
}
