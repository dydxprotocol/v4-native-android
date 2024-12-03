package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.gradient.GradientType
import exchange.dydx.platformui.components.slider.CustomSlider
import exchange.dydx.platformui.components.slider.CustomSliderDefaults
import exchange.dydx.platformui.components.slider.progress
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import kotlin.math.abs

@Preview
@Composable
fun Preview_GradientSlider() {
    DydxThemedPreviewSurface {
        GradientSlider.Content(Modifier, GradientSlider.ViewState.preview)
    }
}

object GradientSlider {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val leftRatio: Float = -0.5f,
        val rightRatio: Float = 1.0f,
        val value: Float = 0.0f,
        val valueRange: ClosedFloatingPointRange<Float>,
        val onValueChange: (Float) -> Unit = {}
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                valueRange = 0.0f..1.0f,
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }
        if (state.leftRatio >= state.rightRatio) {
            return
        }

        val brush: Brush
        val left = abs(state.leftRatio)
        val right = abs(state.rightRatio)
        if (state.leftRatio <= 0 && state.rightRatio >= 0) {
            val totalRatio = left + right
            val leftStep = left / totalRatio
            brush = Brush.linearGradient(
                0.0f to GradientType.MINUS.color.color.copy(alpha = left),
                leftStep to Color.Transparent,
                1.0f to GradientType.PLUS.color.color.copy(alpha = right),
            )
        } else if (state.leftRatio <= 0 && state.rightRatio <= 0) {
            brush = Brush.linearGradient(
                0.0f to GradientType.MINUS.color.color.copy(alpha = left),
                1.0f to GradientType.MINUS.color.color.copy(alpha = right),
            )
        } else {
            brush = Brush.linearGradient(
                0.0f to GradientType.PLUS.color.color.copy(alpha = left),
                1.0f to GradientType.PLUS.color.color.copy(alpha = right),
            )
        }

        CustomSlider(
            value = state.value,
            onValueChange = state.onValueChange,
            valueRange = state.valueRange,
            modifier = modifier,
            showLabel = false,
            showIndicator = false,
            thumb = {
                CustomSliderDefaults.Thumb("")
            },
            track = {
                Box(
                    modifier = Modifier
                        .progress(sliderState = it)
                        .background(
                            brush = brush,
                        ),
                )
            },
        )
    }
}
