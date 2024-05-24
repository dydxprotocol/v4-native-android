package exchange.dydx.trading

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface

@Preview
@Composable
fun PreviewApp() {
    DydxThemedPreviewSurface {
        Text(BuildConfig.VERSION_NAME)
    }
}
