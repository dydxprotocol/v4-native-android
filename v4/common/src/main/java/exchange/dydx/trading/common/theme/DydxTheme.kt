package exchange.dydx.trading.common.theme

import android.content.Context
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Shapes
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.StyleConfig
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeConfig
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeSettings
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.PreviewAppConfig
import exchange.dydx.utilities.utils.JsonUtils
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.JsonNull.content

interface DydxTheme {
    companion object {

        val colors: Colors @Composable get() = MaterialTheme.colors
        val typography: Typography @Composable get() = MaterialTheme.typography
        val shapes: Shapes @Composable get() = MaterialTheme.shapes

        @Composable
        operator fun component1(): Colors = colors

        @Composable
        operator fun component2(): Typography = typography

        @Composable
        operator fun component3(): Shapes = shapes

        fun preview(context: Context): DydxTheme = DydxThemeImpl(
            context = context,
            appConfig = AppConfig.Preview,
        )
    }
}
class DydxThemeImpl(context: Context, appConfig: AppConfig, darkTheme: Boolean = true) : DydxTheme

@Composable
fun DydxThemedPreviewSurface(
    darkTheme: Boolean = true,
    background: ThemeColor.SemanticColor = ThemeColor.SemanticColor.layer_2,
    content: @Composable (AppConfig) -> Unit,
) {
    val appConfig = PreviewAppConfig()
    val appContext = LocalContext.current

    val theme = if (darkTheme) ThemeConfig.dark(appContext) else ThemeConfig.light(appContext)
    val themeConfig = MutableStateFlow<ThemeConfig?>(theme)
    val styleConfig =
        MutableStateFlow<StyleConfig?>(JsonUtils.loadFromAssets(appContext, "dydxStyle.json"))
    ThemeSettings.shared = ThemeSettings(appContext, null, themeConfig, styleConfig)

    Surface(
        color = background.color,
    ) {
        content(appConfig)
    }
}

@Preview
@Composable
private fun DarkPreview() {
    DydxThemedPreviewSurface(darkTheme = true) {
        Text(
            text = "Hello",
            style = TextStyle.dydxDefault
                .themeFont(fontSize = ThemeFont.FontSize.medium)
                .themeColor(ThemeColor.SemanticColor.text_secondary),
        )
    }
}

class MockLocalizer : LocalizerProtocol {
    override fun localize(path: String, paramsAsJson: String?): String {
        return path
    }
}

class MockLogger : Logging {
    override fun d(tag: String, message: String) {
        println("D: $tag: $message")
    }

    override fun e(tag: String, message: String) {
        println("E: $tag: $message")
    }
}
