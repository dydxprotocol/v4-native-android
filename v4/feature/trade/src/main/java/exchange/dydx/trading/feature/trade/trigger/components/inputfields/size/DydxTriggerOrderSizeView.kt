package exchange.dydx.trading.feature.trade.trigger.components.inputfields.size

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.inputs.PlatformSwitchInput
import exchange.dydx.platformui.components.slider.CustomSlider
import exchange.dydx.platformui.components.slider.CustomSliderDefaults
import exchange.dydx.platformui.components.slider.progress
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.navigation.DydxAnimation
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.scarfolds.InputFieldScarfold
import exchange.dydx.trading.feature.shared.views.LabeledTextInput

@Preview
@Composable
fun Preview_DydxTriggerOrderSizeView() {
    DydxThemedPreviewSurface {
        DydxTriggerOrderSizeView.Content(Modifier, DydxTriggerOrderSizeView.ViewState.preview)
    }
}

object DydxTriggerOrderSizeView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val enabled: Boolean = true,
        val onEnabledChanged: (Boolean) -> Unit = {},
        val labeledTextInput: LabeledTextInput.ViewState,
        val percentage: Float = 40.0f,
        val onPercentageChanged: (Float) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                labeledTextInput = LabeledTextInput.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTriggerOrderSizeViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlatformSwitchInput(
                modifier = Modifier.fillMaxWidth(),
                label = state.localizer.localize("APP.GENERAL.CUSTOM_AMOUNT"),
                textStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.base)
                    .themeColor(ThemeColor.SemanticColor.text_secondary),
                value = state.enabled,
                onValueChange = state.onEnabledChanged,
            )

            DydxAnimation.AnimateExpandInOut(
                visible = state.enabled,
            ) {
                SizeContent(modifier, state)
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun SizeContent(modifier: Modifier, state: ViewState) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CustomSlider(
                value = state.percentage,
                onValueChange = state.onPercentageChanged,
                valueRange = 0.0f..100.0f,
                modifier = modifier.weight(1f),
                showLabel = false,
                showIndicator = false,
                thumb = {
                    // CustomSliderDefaults.Thumb("${abs(it)}x")
                    CustomSliderDefaults.Thumb("")
                },
                track = {
                    Box(
                        modifier = Modifier
                            .progress(sliderState = it)
                            .background(
                                brush = Brush.linearGradient(
                                    0.0f to ThemeColor.SemanticColor.text_tertiary.color,
                                    1.0f to ThemeColor.SemanticColor.text_primary.color,
                                ),
                            ),
                    )
                },
            )

            InputFieldScarfold(
                modifier = Modifier.width(120.dp),
            ) {
                LabeledTextInput.Content(
                    modifier = Modifier,
                    state = state.labeledTextInput,
                )
            }
        }
    }
}