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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.navigation.DydxAnimation
import exchange.dydx.trading.feature.shared.scaffolds.InputFieldScaffold
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
        val percentage: Double = 0.4,
        val onPercentageChanged: (Double) -> Unit = {},
        val canEdit: Boolean = true,
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

        val focusManager = LocalFocusManager.current

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
                onValueChange = {
                    if (state.canEdit) {
                        focusManager.clearFocus()
                        state.onEnabledChanged(it)
                    }
                },
                canEdit = state.canEdit,
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
                value = state.percentage.toFloat(),
                onValueChange = {
                    state.onPercentageChanged(it.toDouble())
                },
                valueRange = 0.0f..1.0f,
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

            InputFieldScaffold(
                modifier = Modifier.width(120.dp),
                alertState = state.labeledTextInput.alertState,
            ) {
                LabeledTextInput.Content(
                    modifier = Modifier,
                    state = state.labeledTextInput,
                )
            }
        }
    }
}
