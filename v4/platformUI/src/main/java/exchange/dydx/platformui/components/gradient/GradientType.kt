package exchange.dydx.platformui.components.gradient

import androidx.compose.ui.graphics.Brush
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.negativeGradient
import exchange.dydx.platformui.designSystem.theme.noGradient
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.platformui.designSystem.theme.positiveGradient

enum class GradientType {
    NONE,
    PLUS,
    MINUS;

    val color: ThemeColor.SemanticColor
        get() = when (this) {
            NONE -> ThemeColor.SemanticColor.transparent
            PLUS -> ThemeColor.SemanticColor.positiveColor
            MINUS -> ThemeColor.SemanticColor.negativeColor
        }

    fun brush(backgroundColor: ThemeColor.SemanticColor): Brush {
        return when (this) {
            NONE -> backgroundColor.noGradient
            PLUS -> backgroundColor.positiveGradient
            MINUS -> backgroundColor.negativeGradient
        }
    }
}
