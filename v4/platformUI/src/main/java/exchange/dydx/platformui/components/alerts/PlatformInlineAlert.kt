package exchange.dydx.platformui.components.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont

object PlatformInlineAlert {
    enum class Level {
        WARNING,
        ERROR,
    }
}

private val PlatformInlineAlert.Level.color: Color
    get() = when (this) {
        PlatformInlineAlert.Level.WARNING -> ThemeColor.SemanticColor.color_yellow.color
        PlatformInlineAlert.Level.ERROR -> ThemeColor.SemanticColor.color_red.color
    }

private val PlatformInlineAlert.Level.backgroundColor: Color
    get() = when (this) {
        PlatformInlineAlert.Level.WARNING -> ThemeColor.SemanticColor.color_faded_yellow.color
        PlatformInlineAlert.Level.ERROR -> ThemeColor.SemanticColor.color_faded_red.color
    }

@Composable
fun PlatformInlineAlert(
    text: String,
    level: PlatformInlineAlert.Level,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .clip(RoundedCornerShape(4.dp))
            .background(level.backgroundColor)
            .height(IntrinsicSize.Min),
    ) {
        Box(
            Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(level.color),
        )

        Text(
            text = text,
            style = TextStyle.dydxDefault.themeFont(fontSize = ThemeFont.FontSize.small).themeColor(ThemeColor.SemanticColor.color_white),
            modifier = Modifier
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.VerticalPadding,
                ),
        )
    }
}
