package exchange.dydx.platformui.designSystem.theme

import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import exchange.dydx.utilities.utils.JsonUtils
import exchange.dydx.utilities.utils.Logging
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

class ThemeSettings(
    private val context: Context,
    private val sharedPreferences: SharedPreferencesStore?,
    val themeConfig: MutableStateFlow<ThemeConfig?> = MutableStateFlow(ThemeConfig.sampleThemeConfig(context)),
    val styleConfig: MutableStateFlow<StyleConfig?> = MutableStateFlow(StyleConfig.sampleStyleConfig(context)),
) {

    val greenIsUp: Boolean
        get() = (
            sharedPreferences?.read("direction_color_preference")
                ?: "green_is_up"
            ) == "green_is_up"
    companion object {
        lateinit var shared: ThemeSettings

        fun defaultThemeSettings(
            context: Context,
            sharedPreferences: SharedPreferencesStore
        ): ThemeSettings {
            val themeConfig = MutableStateFlow<ThemeConfig?>(ThemeConfig.sampleThemeConfig(context))
            val styleConfig = MutableStateFlow<StyleConfig?>(StyleConfig.sampleStyleConfig(context))
            return ThemeSettings(context, sharedPreferences, themeConfig, styleConfig)
        }
    }
    var respondsToSystemTheme = true
}

@Serializable
data class ThemeConfig(
    val themeColor: ThemeColor,
    val themeFont: ThemeFont,
    val id: String? = null,
) {
    companion object {
        private const val TAG = "ThemeConfig"

        fun sampleThemeConfig(context: Context): ThemeConfig? = JsonUtils.loadFromAssets(context, "SampleTheme.json")

        fun dark(context: Context): ThemeConfig? = JsonUtils.loadFromAssets(context, "ThemeDark.json")
        fun light(context: Context): ThemeConfig? = JsonUtils.loadFromAssets(context, "ThemeLight.json")
        fun classicDark(context: Context): ThemeConfig? = JsonUtils.loadFromAssets(context, "ThemeClassicDark.json")

        fun createFromPreference(
            context: Context,
            preference: String,
            logger: Logging,
        ): ThemeConfig? {
            val config = when (preference) {
                "dark" -> dark(context)
                "light" -> light(context)
                "classic_dark" -> classicDark(context)
                "system" -> if (context.isDarkThemeOn()) dark(context) else light(context)
                else -> null
            }

            return if (config == null) {
                logger.e(TAG, "config is null")
                sampleThemeConfig(context)
            } else {
                config
            }
        }

        private fun Context.isDarkThemeOn(): Boolean {
            return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
        }
    }
}

@Serializable
data class StyleConfig(
    val styles: Map<String, ThemeStyle>,
) {
    companion object {
        fun sampleStyleConfig(context: Context): StyleConfig? = JsonUtils.loadFromAssets(context, "SampleStyle.json")
    }
}

@Serializable
data class ThemeStyle(
    val _fontSize: String? = null,
    val _fontType: String? = null,
    val _layerColor: String? = null,
    val _textColor: String? = null,
) {
    companion object {
        fun defaultStyle(themeSettings: ThemeSettings): ThemeStyle = themeSettings.styleConfig.value?.styles?.get("default-style")
            ?: ThemeStyle()
    }

    var fontSize: ThemeFont.FontSize?
        get() = this._fontSize?.let { ThemeFont.FontSize.valueOf(it) }
        set(value) {
            this.copy(_fontSize = value?.name)
        }

    var fontType: ThemeFont.FontType?
        get() = this._fontType?.let { ThemeFont.FontType.valueOf(it) }
        set(value) {
            this.copy(_fontType = value?.name)
        }

    var layerColor: ThemeColor.SemanticColor?
        get() = this._layerColor?.let { ThemeColor.SemanticColor.valueOf(it) }
        set(value) {
            this.copy(_layerColor = value?.name)
        }

    var textColor: ThemeColor.SemanticColor?
        get() = this._textColor?.let { ThemeColor.SemanticColor.valueOf(it) }
        set(value) {
            this.copy(_textColor = value?.name)
        }

    fun merge(from: ThemeStyle): ThemeStyle {
        return this.copy(
            _fontSize = from._fontSize ?: this._fontSize,
            _fontType = from._fontType ?: this._fontType,
            _layerColor = from._layerColor ?: this._layerColor,
            _textColor = from._textColor ?: this._textColor,
        )
    }

    fun themeColor(foreground: ThemeColor.SemanticColor? = null, background: ThemeColor.SemanticColor? = null): ThemeStyle {
        var style: ThemeStyle = this
        if (foreground != null) {
            style = merge(from = ThemeStyle(_textColor = foreground.name.lowercase()))
        }
        if (background != null) {
            style = style.merge(from = ThemeStyle(_layerColor = background.name.lowercase()))
        }
        return style
    }

    fun themeFont(fontType: ThemeFont.FontType? = null, fontSize: ThemeFont.FontSize = ThemeFont.FontSize.base): ThemeStyle {
        return this.merge(from = ThemeStyle(_fontSize = fontSize.name.lowercase(), _fontType = fontType?.name?.lowercase()))
    }
}

@Serializable
data class ThemeColor(
    val color: Map<SemanticColor, String>,
) {
    @Serializable
    enum class SemanticColor(val rawValue: String) {
        transparent("transparent"),
        text_primary("text_primary"),
        text_secondary("text_secondary"),
        text_tertiary("text_tertiary"),

        layer_0("layer_0"),
        layer_1("layer_1"),
        layer_2("layer_2"),
        layer_3("layer_3"),
        layer_4("layer_4"),
        layer_5("layer_5"),
        layer_6("layer_6"),
        layer_7("layer_7"),

        border_default("border_default"),
        border_destructive("border_destructive"),
        border_button("border_button"),

        color_purple("color_purple"),
        color_yellow("color_yellow"),
        color_green("color_green"),
        color_red("color_red"),
        color_white("color_white"),
        color_black("color_black"),
        color_faded_green("color_faded_green"),
        color_faded_red("color_faded_red"),
        color_faded_yellow("color_faded_yellow"),
        color_faded_purple("color_faded_purple"),

        gradient_gray_start("gradient_gray_start"),
        gradient_gray_end("gradient_gray_end"),
        gradient_green_start("gradient_green_start"),
        gradient_green_end("gradient_green_end"),
        gradient_red_start("gradient_red_start"),
        gradient_red_end("gradient_red_end");

        companion object {
        }
    }
}

@Serializable
data class ThemeFont(
    val size: Map<FontSize, String>,
    val type: Map<FontType, FontTypeDetail>,
) {
    @Serializable
    enum class FontSize(private val rawValue: String) {
        extra("extra"),
        large("large"),
        medium("medium"),
        base("base"),
        small("small"),
        mini("mini"),
        tiny("tiny"),
        custom("")
    }

    @Serializable
    enum class FontType(private val rawValue: String) {
        plus("plus"),
        book("book"),
        minus("minus"),
        number("number"),
        custom("")
    }
}

@Serializable
data class FontTypeDetail(
    val name: String,
    val weight: Float,
)
