package exchange.dydx.platformui.designSystem.theme

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import exchange.dydx.platformui.R

private var _colorMap: MutableMap<ThemeColor.SemanticColor, Color> = mutableMapOf()

var ThemeSettings.colorMap: Map<ThemeColor.SemanticColor, Color>
    get() = _colorMap
    set(value) {
        _colorMap = value.toMutableMap()
    }

fun ThemeSettings.colorOf(token: ThemeColor.SemanticColor): Color = getColor(token) ?: Color.Transparent

val ThemeColor.SemanticColor.color: Color get() = ThemeSettings.shared.colorOf(this)

fun Modifier.themeColor(
    background: ThemeColor.SemanticColor,
): Modifier {
    val color = getColor(background)
    return if (color != null) {
        this.then(Modifier.background(color))
    } else {
        this
    }
}

fun TextStyle.themeColor(
    foreground: ThemeColor.SemanticColor,
): TextStyle {
    val color = getColor(foreground)
    return if (color != null) {
        this.copy(color = color)
    } else {
        this
    }
}

private fun getColor(token: ThemeColor.SemanticColor): Color? {
    val theme = ThemeSettings.shared.themeConfig.value
    val color: Color?
    if (_colorMap.containsKey(token)) {
        color = _colorMap[token]!!
    } else {
        val colorString = theme?.themeColor?.color?.get(token)
        val parsedColor = colorString?.let { parseColor(it) }
        if (parsedColor != null) {
            color = parsedColor
            _colorMap[token] = color
        } else {
            color = null
        }
    }
    return color
}

private fun parseColor(colorString: String): Color {
    if (colorString.startsWith("#") && colorString.length == 9) {
        val rrggbb = colorString.substring(1, 7)
        val aa = colorString.substring(7, 9)
        val aarrggbb = "#$aa$rrggbb"
        return Color(android.graphics.Color.parseColor(aarrggbb))
    }
    return Color(android.graphics.Color.parseColor(colorString))
}

private fun ThemeFont.unit(
    fontSize: ThemeFont.FontSize?,
): TextUnit {
    val sizeValue = size[fontSize]?.toFloat()
    return sizeValue?.sp ?: 16.sp
}
val TextStyle.Companion.dydxDefault: TextStyle
    get() {
        val style = ThemeStyle.defaultStyle(ThemeSettings.shared)
        val theme = ThemeSettings.shared.themeConfig.value
        return TextStyle(
            fontFamily = family(style.fontType ?: ThemeFont.FontType.book),
            fontSize = theme?.themeFont?.unit(style.fontSize ?: ThemeFont.FontSize.base) ?: 16.sp,
            color = getColor(ThemeColor.SemanticColor.text_secondary) ?: Color.Black,
        )
    }

fun TextStyle.themeFont(
    fontType: ThemeFont.FontType? = null,
    fontSize: ThemeFont.FontSize? = null,
): TextStyle {
    var value = this
    if (fontType != null) {
        value = value.copy(fontFamily = family(fontType))
    }
    val themeFont = ThemeSettings.shared.themeConfig.value?.themeFont
    if (fontSize != null && themeFont != null) {
        value = value.copy(fontSize = themeFont.unit(fontSize))
    }
    return value
}

fun TextStyle.themeFont(
    fontType: ThemeFont.FontType? = null,
    rawSize: Double,
): TextStyle {
    var value = this
    if (fontType != null) {
        value = value.copy(fontFamily = family(fontType))
    }
    value = value.copy(fontSize = rawSize.sp)
    return value
}

private fun family(
    fontType: ThemeFont.FontType,
): FontFamily {
    val theme = ThemeSettings.shared.themeConfig.value
    val fontDetails = theme?.themeFont?.type?.get(fontType)
    if (fontDetails != null) {
        val weight = FontWeight((fontDetails.weight * 1000).toInt())
        return when (fontDetails.name) {
            "Satoshi-Bold" -> FontFamily(Font(R.font.satoshi_bold, weight))
            "Satoshi-Regular" -> FontFamily(Font(R.font.satoshi_regular, weight))
            "Satoshi-Medium" -> FontFamily(Font(R.font.satoshi_medium, weight))
            "RobotoMono-Regular" -> FontFamily(Font(R.font.roboto_mono_regular, weight))
            else -> {
                FontFamily(Font(R.font.satoshi_regular, weight))
            }
        }
    }
    return FontFamily(Font(R.font.satoshi_regular))
}

val ThemeColor.SemanticColor.Companion.positiveColor: ThemeColor.SemanticColor
    get() = if (ThemeSettings.shared.greenIsUp) {
        ThemeColor.SemanticColor.color_green
    } else {
        ThemeColor.SemanticColor.color_red
    }

val ThemeColor.SemanticColor.Companion.negativeColor: ThemeColor.SemanticColor
    get() = if (ThemeSettings.shared.greenIsUp) {
        ThemeColor.SemanticColor.color_red
    } else {
        ThemeColor.SemanticColor.color_green
    }

val ThemeColor.SemanticColor.Companion.textOnPositiveColor: ThemeColor.SemanticColor
    get() = if (ThemeSettings.shared.greenIsUp) {
        ThemeColor.SemanticColor.color_black
    } else {
        ThemeColor.SemanticColor.color_white
    }

val ThemeColor.SemanticColor.Companion.textOnNegativeColor: ThemeColor.SemanticColor
    get() = if (ThemeSettings.shared.greenIsUp) {
        ThemeColor.SemanticColor.color_white
    } else {
        ThemeColor.SemanticColor.color_black
    }

val ThemeColor.SemanticColor.positiveGradient: Brush
    get() {
        val colorStops = arrayOf(
            0.0f to this.color,
            1f to ThemeColor.SemanticColor.positiveColor.color.copy(alpha = 0.1f),
        )
        return Brush.horizontalGradient(colorStops = colorStops)
    }

val ThemeColor.SemanticColor.negativeGradient: Brush
    get() {
        val colorStops = arrayOf(
            0.0f to this.color,
            1f to ThemeColor.SemanticColor.negativeColor.color.copy(alpha = 0.1f),
        )
        return Brush.horizontalGradient(colorStops = colorStops)
    }

val ThemeColor.SemanticColor.noGradient: Brush
    get() {
        val colorStops = arrayOf(
            0.0f to this.color,
            0.0f to this.color,
        )
        return Brush.horizontalGradient(colorStops = colorStops)
    }

fun ThemeSettings.isLightTheme(): Boolean {
    return ThemeColor.SemanticColor.layer_0.color.luminance() > 0.5f
}
