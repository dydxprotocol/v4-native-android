package exchange.dydx.platformui.components.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlatformTextInput(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    label: @Composable (() -> Unit)? = null,
    value: String? = null,
    textStyle: TextStyle =
        TextStyle.dydxDefault
            .themeFont(
                fontSize = ThemeFont.FontSize.medium,
                fontType = ThemeFont.FontType.number,
            ),
    alertState: PlatformInputAlertState = PlatformInputAlertState.None,
    placeHolder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onValueChange: (String) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (label != null) {
                label()
            }
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()
            val currentValue = remember { mutableStateOf<String?>(value) } // value during editing
            if (!isFocused) {
                currentValue.value = value
            }
            val displayValue = if (isFocused) currentValue.value ?: "" else value ?: ""
            val textColor = if (isFocused) ThemeColor.SemanticColor.text_primary else alertState.textColor

            BasicTextField(
                modifier = Modifier.focusRequester(focusRequester = focusRequester),
                value = displayValue,
                onValueChange = {
                    currentValue.value = it
                    onValueChange(it)
                },
                singleLine = true,
                keyboardOptions = keyboardOptions,
                interactionSource = interactionSource,
                textStyle = textStyle
                    .themeColor(textColor),
                cursorBrush = SolidColor(ThemeColor.SemanticColor.text_primary.color),
                decorationBox = { innerTextField ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (displayValue.isEmpty()) {
                            Text(
                                text = placeHolder ?: "",
                                style = textStyle
                                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                                maxLines = 1,
                            )
                        }
                    }
                    TextFieldDefaults.TextFieldDecorationBox(
                        value = displayValue,
                        visualTransformation = VisualTransformation.None,
                        innerTextField = innerTextField,
                        singleLine = true,
                        enabled = true,
                        interactionSource = interactionSource,
                        contentPadding = PaddingValues(0.dp),
                    )
                },
            )
        }
    }
}
