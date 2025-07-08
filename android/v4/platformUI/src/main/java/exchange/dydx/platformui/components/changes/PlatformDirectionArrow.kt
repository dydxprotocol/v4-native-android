package exchange.dydx.platformui.components.changes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import exchange.dydx.platformui.R
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor

enum class PlatformDirection {
    Up, Down, None, Hide;

    companion object {
        fun from(value1: Double?, value2: Double?): PlatformDirection {
            return when {
                value1 == null || value2 == null -> Hide
                value1 > value2 -> Down
                value1 < value2 -> Up
                else -> None
            }
        }
    }
}

@Composable
fun PlatformDirectionArrow(
    modifier: Modifier = Modifier,
    direction: PlatformDirection = PlatformDirection.None,
) {
    if (direction == PlatformDirection.Hide) return
    PlatformImage(
        icon = R.drawable.icon_arrow,
        modifier = modifier,
        colorFilter = when (direction) {
            PlatformDirection.Up -> ColorFilter.tint(color = ThemeColor.SemanticColor.positiveColor.color)
            PlatformDirection.Down -> ColorFilter.tint(color = ThemeColor.SemanticColor.negativeColor.color)
            PlatformDirection.None -> ColorFilter.tint(color = ThemeColor.SemanticColor.text_tertiary.color)
            else -> null
        },
    )
}
