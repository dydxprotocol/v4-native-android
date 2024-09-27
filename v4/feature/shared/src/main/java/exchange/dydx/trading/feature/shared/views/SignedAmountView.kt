package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import kotlin.math.sign

@Preview
@Composable
fun Preview_SignedAmountView() {
    DydxThemedPreviewSurface {
        SignedAmountView.Content(Modifier, SignedAmountView.ViewState.preview)
    }
}

object SignedAmountView {
    enum class ColoringOption {
        SignOnly, TextOnly, AllText
    }

    data class ViewState(
        val text: String?,
        val sign: PlatformUISign = PlatformUISign.None,
        val coloringOption: ColoringOption = ColoringOption.AllText,
    ) {
        companion object {
            val preview = ViewState(
                text = "1.0M",
            )

            fun fromDouble(value: Double?, formatting: ((Double?) -> String)): ViewState {
                if (value == null) {
                    return ViewState(
                        text = "-",
                    )
                } else {
                    val sign = when (sign(value)) {
                        1.0 -> PlatformUISign.Plus
                        -1.0 -> PlatformUISign.Minus
                        else -> PlatformUISign.None
                    }
                    return ViewState(
                        text = formatting(value),
                        sign = sign,
                    )
                }
            }
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?, textStyle: TextStyle = TextStyle.dydxDefault) {
        if (state?.text == null) {
            return
        }
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (state.coloringOption) {
                ColoringOption.SignOnly, ColoringOption.AllText -> {
                    when (state.sign) {
                        PlatformUISign.Plus -> {
                            Text(
                                text = "+",
                                style = textStyle.themeColor(ThemeColor.SemanticColor.positiveColor),
                            )
                        }

                        PlatformUISign.Minus -> {
                            Text(
                                text = "-",
                                style = textStyle.themeColor(ThemeColor.SemanticColor.negativeColor),
                            )
                        }

                        PlatformUISign.None -> {
                            Text(
                                text = "",
                            )
                        }
                    }
                }

                ColoringOption.TextOnly -> {
                    Text(
                        text = "",
                    )
                }
            }

            when (state.coloringOption) {
                ColoringOption.SignOnly -> {
                    Text(
                        text = state.text,
                        style = textStyle,
                    )
                }

                ColoringOption.AllText, ColoringOption.TextOnly -> {
                    when (state.sign) {
                        PlatformUISign.Plus -> {
                            Text(
                                text = state.text,
                                style = textStyle.themeColor(ThemeColor.SemanticColor.positiveColor),
                            )
                        }

                        PlatformUISign.Minus -> {
                            Text(
                                text = state.text,
                                style = textStyle.themeColor(ThemeColor.SemanticColor.negativeColor),
                            )
                        }

                        PlatformUISign.None -> {
                            Text(
                                text = state.text,
                                style = textStyle,
                            )
                        }
                    }
                }
            }
        }
    }
}
