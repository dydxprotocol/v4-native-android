package exchange.dydx.platformui.components.progress

import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color

@Composable
fun PlatformIndeterminateProgress(
    modifier: Modifier = Modifier,
    size: Dp,
    outerTrackColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.text_tertiary,
    trackColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.color_purple,
) {
    CircularProgressIndicator(
        modifier = modifier.width(size),
        color = trackColor.color,
        trackColor = outerTrackColor.color,
    )
}
