package exchange.dydx.integration.javascript

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.BuildConfig
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG: String = "JavascriptRunner(V4)"
private const val DO_LOG: Boolean = AppConfig.VERBOSE_LOGGING

class JavascriptRunnerV4 constructor(
    private val scriptDescription: String,
    private val scriptInitializationCode: String,
    private val scope: CoroutineScope,
    private val logger: Logging,
) : JavascriptRunner {

    override val initialized = MutableStateFlow(false)

    private var callbackMap: MutableMap<String, ResultCallback> = mutableMapOf()

    companion object {
        fun runnerFromFile(
            scope: CoroutineScope,
            context: Context,
            file: String,
            logger: Logging,
        ): JavascriptRunnerV4? {
            val script = JavascriptUtils.loadAsset(context, file)
            if (script != null) {
                return JavascriptRunnerV4(file, script, scope, logger)
            }
            return null
        }
    }

    @JavascriptInterface
    fun onJsAsyncResult(key: String, result: String?) {
        val callback: ResultCallback?
        synchronized(callbackMap) {
            callback = callbackMap[key]

            if (callback == null) {
                logger.e(TAG, "No callback found for key: $key")
                return
            }
            callbackMap.remove(key)
        }
        callback?.invoke(JavascriptRunnerResult(response = result))
    }

    override var webview: WebView? = null
        set(value) {
            logger.d(TAG, "Setting Webview")
            if (field == value) {
                logger.e(TAG, "Noop update")
                return
            }
            if (field != null) {
                logger.e(TAG, "Updating webview in runner")
            }
            initialized.value = false
            if (value != null) {
                //    value.settings.javaScriptEnabled = true
                logger.d(TAG, "Loading base url")
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
                logger.d(TAG, "Error in webview client: $error")
            }

            override fun onPageFinished(view: WebView, weburl: String) {
                initializeJavascriptEnvironment() {
                    logger.d(TAG, "Initialized javascript environment: $it")
                    initialized.value = true
                }
            }
        }
    }

    override val webappInterface: WebAppInterface = WebAppInterface()

    override fun runJs(function: String, params: List<String>, callback: ResultCallback) {
        val tranformedParams: MutableList<String> = params.toMutableList()
        val paramsText = tranformedParams.joinToString(",")

        val key = "key" + UUID.randomUUID().toString()
        synchronized(callbackMap) {
            callbackMap[key] = callback
        }

        val script = """
            function bridgeFunction() {
               try {
                   $function($paramsText).then(function(result) {
                      bridge.onJsAsyncResult("$key", result);
                   });
               } catch (e) {
                   bridge.onJsAsyncResult("$key", e.toString());
               }
            };

            bridgeFunction();
        """.trimIndent()

        launchJs(script, callback = null) // no callback, since we let onJsAsyncResult() handle it
    }

    override fun runJs(script: String, callback: ResultCallback) {
        launchJs(script, callback)
    }

    fun initializeJavascriptEnvironment(callback: ResultCallback) {
        launchJs(scriptInitializationCode, callback)
    }

    private fun launchJs(script: String, callback: ResultCallback?) {
        val length = if (BuildConfig.DEBUG) 1024 else 32
        val localWebview = webview
        if (localWebview == null) {
            logger.e(
                TAG,
                "Unable to run function, no webview present, ${script.take(length)}",
            )
            return
        }
        logger.d(TAG, "Running script: ${script.take(length)}")

        scope.launch {
            try {
                // for production, only log the first 32 characters to avoid logging sensitive data
                localWebview.evaluateJavascript(
                    script,
                ) { evalResult ->
                    if (callback != null) {
                        logger.d(
                            TAG,
                            "Script completed: result = $evalResult from  ${script.take(32)}",
                        )
                        val result = evalResult.removeSurrounding("\"")
                        if (result != evalResult) {
                            logger.d(TAG, "Stripped surrounding quotes")
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
                }
            } catch (e: Exception) {
                logger.e(TAG, "Error executing script:  ${script.take(32)}, error: $e")
            }
        }
    }
}
