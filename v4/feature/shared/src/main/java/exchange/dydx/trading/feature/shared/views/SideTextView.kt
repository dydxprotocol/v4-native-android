package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer

@Preview
@Composable
fun Preview_SideTextView() {
    DydxThemedPreviewSurface {
        SideTextView.Content(Modifier, SideTextView.ViewState.preview)
    }
}

object SideTextView {

    enum class ColoringOption {
        NONE,
        COLORED,
        WITH_BACKGROUND,
    }

    sealed class Side {
        abstract val stringKey: String

        object Long : Side() {
            override val stringKey: String
                get() = "APP.GENERAL.LONG_POSITION_SHORT"
        }

        object Short : Side() {
            override val stringKey: String
                get() = "APP.GENERAL.SHORT_POSITION_SHORT"
        }

        object Buy : Side() {
            override val stringKey: String
                get() = "APP.GENERAL.BUY"
        }

        object Sell : Side() {
            override val stringKey: String
                get() = "APP.GENERAL.SELL"
        }

        object None : Side() {
            override val stringKey: String
                get() = "APP.GENERAL.NONE"
        }

        class Custom(val customText: String) : Side() {
            override val stringKey: String
                get() = customText
        }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val coloringOption: ColoringOption = ColoringOption.COLORED,
        val side: Side = Side.Custom("Unknown"),
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                coloringOption = ColoringOption.WITH_BACKGROUND,
                side = Side.Custom("Short"),
            )
        }
    }

    @Composable
    fun Content(
        modifier: Modifier,
        state: ViewState?,
        textStyle: TextStyle = TextStyle.dydxDefault,
    ) {
        if (state == null) {
            return
        }
        val textColor = when (state.side) {
            Side.Long, Side.Buy -> ThemeColor.SemanticColor.positiveColor
            Side.Short, Side.Sell -> ThemeColor.SemanticColor.negativeColor
            else -> ThemeColor.SemanticColor.text_primary
        }
        val layerColor = when (state.side) {
            Side.Long, Side.Buy -> ThemeColor.SemanticColor.positiveColor
            Side.Short, Side.Sell -> ThemeColor.SemanticColor.negativeColor
            else -> ThemeColor.SemanticColor.layer_6
        }
        when (state.coloringOption) {
            ColoringOption.NONE -> {
                Text(
                    text = state.localizer.localize(state.side.stringKey),
                    style = textStyle,
                )
            }

            ColoringOption.COLORED -> {
                Text(
                    text = state.localizer.localize(state.side.stringKey),
                    style = textStyle.themeColor(textColor),
                )
            }

            ColoringOption.WITH_BACKGROUND -> {
                Column(
                    modifier = modifier
                        .background(
                            color = layerColor.color.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp),
                        )
                        .padding(horizontal = 4.dp)
                        .padding(vertical = 2.dp),
                ) {
                    Text(
                        text = state.localizer.localize(state.side.stringKey),
                        style = textStyle.themeColor(textColor),
                    )
                }
            }
        }
    }
}
