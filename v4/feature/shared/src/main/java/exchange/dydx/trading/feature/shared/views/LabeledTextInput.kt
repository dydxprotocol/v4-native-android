package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.inputs.PlatformInputAlertState
import exchange.dydx.platformui.components.inputs.PlatformTextInput
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer

@Preview
@Composable
fun Preview_LabeledTextInput() {
    DydxThemedPreviewSurface {
        LabeledTextInput.Content(Modifier, LabeledTextInput.ViewState.preview)
    }
}

object LabeledTextInput {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val label: String? = null,
        val token: String? = null,
        val value: String? = null,
        val placeholder: String? = null,
        val onValueChanged: (String) -> Unit = {},
        val alertState: PlatformInputAlertState = PlatformInputAlertState.None,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                label = "Limit Price",
                token = "USD",
                value = "1.0M",
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        PlatformTextInput(
            modifier = modifier
                .padding(ThemeShapes.InputPaddingValues)
                .heightIn(min = ThemeShapes.InputHeight),
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (state.label != null) {
                        Text(
                            text = state.label,
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.mini)
                                .themeColor(ThemeColor.SemanticColor.text_tertiary),
                        )
                    }

                    if (state.token != null) {
                        TokenTextView.Content(
                            modifier = Modifier,
                            state = TokenTextView.ViewState(symbol = state.token),
                            textStyle = TextStyle.dydxDefault
                                .themeFont(
                                    fontSize = ThemeFont.FontSize.tiny,
                                    fontType = ThemeFont.FontType.minus,
                                )
                                .themeColor(ThemeColor.SemanticColor.text_primary),
                        )
                    }
                }
            },
            value = state.value,
            alertState = state.alertState,
            placeHolder = state.placeholder,
            onValueChange = state.onValueChanged,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}
