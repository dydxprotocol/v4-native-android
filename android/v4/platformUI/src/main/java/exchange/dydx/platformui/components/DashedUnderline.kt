package exchange.dydx.platformui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect

fun Modifier.dashedUnderline(
    color: Color = Color.Black,
    strokeWidth: Float = 2f,
    dashLength: Float = 10f,
    gapLength: Float = 10f
) = this.drawBehind {
    val y = size.height // baseline for underline
    drawLine(
        color = color,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = strokeWidth,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f),
    )
}
