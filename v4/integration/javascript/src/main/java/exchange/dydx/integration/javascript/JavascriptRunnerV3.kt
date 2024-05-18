package exchange.dydx.integration.javascript

import android.content.Context
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.InputStream

private const val TAG: String = "JavascriptRunner(V3)"

private fun loadAsset(context: Context, fileName: String?): String? {
    if (fileName != null) {
        var text: String? = null
        try {
            val inputStream: InputStream = context.assets.open(fileName)
            text = inputStream.bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return text
    }
    return null
}

class JavascriptRunnerV3 constructor(
    private val scriptDescription: String,
    private val scriptInitializationCode: String,
    private val scope: CoroutineScope,
) : JavascriptRunner {

    override val initialized = MutableStateFlow(false)

    companion object {
        fun runnerFromFile(
            scope: CoroutineScope,
            context: Context,
            file: String,
        ): JavascriptRunnerV3? {
            val script = loadAsset(context, file)
            if (script != null) {
                return JavascriptRunnerV3(file, script, scope)
            }
            return null
        }
    }

    override var webview: WebView? = null
        set(value) {
            Timber.tag(TAG).d("Setting Webview")
            if (field == value) {
                Timber.tag(TAG).d("Noop update")
                return
            }
            if (field != null) {
                Timber.tag(TAG).w("Updating webview in runner")
            }
            initialized.value = false
            if (value != null) {
//                value.settings.javaScriptEnabled = true
                Timber.tag(TAG).d("Loading base url")
            }

            field = value
        }

    override val webappInterface: WebAppInterface = WebAppInterface()

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
                scope.launch {
                    initializeJavascriptEnvironment()
                }
            }
        }
    }

    override fun runJs(function: String, params: List<String>, callback: ResultCallback) {
        throw JavascriptApiException("Not supported")
    }

    override fun runJs(script: String, callback: ResultCallback) {
        val webview = this.webview
        if (webview == null) {
            Timber.tag(TAG).w("Unable to run script, assign a webview first.\n%s", script)
            return
        }
        try {
            webview.evaluateJavascript(script) { resultString: String ->
                try {
                    Timber.tag(TAG).i("Evaluated javascript: %s", resultString)
                    callback.invoke(JavascriptRunnerResult(resultString))
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e)
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e)
        }
    }

    suspend fun runSetup(callback: ResultCallback) {
        Timber.tag(TAG).d("Initializing: %s", scriptDescription)
        runJs(script = scriptInitializationCode, callback = callback)
    }

    suspend fun testEcho(input: String, callback: ResultCallback) {
        runJs(script = "testEcho('$input')", callback = callback)
    }

    private suspend fun initializeJavascriptEnvironment() {
        Timber.tag(TAG).d("Page finished loading")
        testEcho("Hello, dYdX") { echo ->
            Timber.tag(TAG).d("Echo: %s", echo?.toString())
            if (echo == null) {
                Timber.tag(TAG).e("Echo failed in: %s", scriptDescription)
                return@testEcho
            }
            Timber.tag(TAG).d("Page initialized: %s", scriptDescription)
            runBlocking {
                runSetup { res ->
                    Timber.tag(TAG).d("Setup complete: %s", res?.toString())
                    runBlocking {
                        runJs("var helper = new StarkHelper.StarkHelper()") {
                            Timber.tag(TAG).i("Helper initialized: %s", it)
                            initialized.value = true
                        }
                    }
                }
            }
        }
    }
}
