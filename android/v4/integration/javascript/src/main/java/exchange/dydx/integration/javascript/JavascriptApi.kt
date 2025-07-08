package exchange.dydx.integration.javascript

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isVisible
import exchange.dydx.trading.common.DydxException
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

private const val TAG = "JavascriptApi"

interface JavascriptRunner : JsEngine {
    val initialized: StateFlow<Boolean>

    var webview: WebView?
    fun webviewClient(): WebViewClient

    val webappInterface: WebAppInterface
}

class WebAppInterface(val interfaceName: String = "Callback") {
    var callback: ResultCallback? = null

    @JavascriptInterface
    fun callbackFunction(text: String) {
        val callback = this.callback
        if (callback == null) {
            Timber.tag(TAG).w("No callback detected for result: %s", text)
            return
        }
        Timber.tag(TAG).d("Received callback: %s", text)
        callback.invoke(JavascriptRunnerResult(text))
    }
}

class JavascriptApiException(msg: String, cause: Throwable? = null) : DydxException(msg, cause)

typealias ResultCallback = (result: JavascriptRunnerResult?) -> Unit
data class JavascriptRunnerResult(val response: String?, val initializing: Boolean = false)
interface JavascriptApi {

    val description: String
    val runner: JavascriptRunner
}
open class JavascriptApiImpl(context: Context, override val description: String, override val runner: JavascriptRunner) : JavascriptApi

private const val indexFile = "file:///android_asset/index.html"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun JavascriptRunnerWebview(
    modifier: Modifier = Modifier,
    layoutParams: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
    ),
    isVisible: Boolean = false,
    javascriptRunner: JavascriptRunner,
    logger: Logging?,
) {
    val webappInterface = javascriptRunner.webappInterface
    AndroidView(
        modifier = modifier,
        factory = {
            WebView(it).apply {
                this.layoutParams = layoutParams
                this.isVisible = isVisible
                this.webViewClient = javascriptRunner.webviewClient()
                this.settings.javaScriptEnabled = true
                this.addJavascriptInterface(webappInterface, webappInterface.interfaceName)
            }
        },
        update = {
            if (javascriptRunner.webview == it) {
                logger?.d(TAG, "Webview not updated")
                return@AndroidView
            }
            logger?.d(TAG, "Webview updated: $it")
            javascriptRunner.webview = it
            it.loadUrl(indexFile)
        },
    )
}
