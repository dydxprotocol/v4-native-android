package exchange.dydx.platformui.components.progress

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color
import kotlin.math.min

@Composable
fun PlatformCircularProgress(
    modifier: Modifier = Modifier,
    progress: Double,
    outerTrackColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.color_red,
    innerTrackColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.color_red,
    lineWidth: Double = 20.0,
) {
    Canvas(
        modifier,
    ) {
        // Outer track
        drawCircle(
            color = outerTrackColor.color.copy(alpha = 0.2f),
            radius = size.width / 2f,
            style = Stroke(lineWidth.dp.toPx()),
        )

        // Inner track
        val sweepAngle = (360 * min(progress, 1.0))
        drawArc(
            color = innerTrackColor.color,
            startAngle = -90f,
            sweepAngle = sweepAngle.toFloat(),
            useCenter = false,
            style = Stroke(lineWidth.dp.toPx(), cap = Stroke.DefaultCap),
        )
    }
}
