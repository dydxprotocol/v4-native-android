package exchange.dydx.platformui.components

import android.graphics.Color
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun PlatformWebView(
    modifier: Modifier,
    url: String,
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(Color.BLACK)
                loadUrl(url)
            }
        },
    )
}
