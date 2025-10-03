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
import androidx.compose.ui.text.style.TextAlign
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
    centeredText: Boolean = false,
) {
    if (centeredText) {
        TextFieldContent(
            focusRequester = focusRequester,
            value = value,
            textStyle = textStyle,
            alertState = alertState,
            placeHolder = placeHolder,
            keyboardOptions = keyboardOptions,
            onValueChange = onValueChange,
            centeredText = centeredText,
        )
        return
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (label != null) {
            label()
        }

        TextFieldContent(
            focusRequester = focusRequester,
            value = value,
            textStyle = textStyle,
            alertState = alertState,
            placeHolder = placeHolder,
            keyboardOptions = keyboardOptions,
            onValueChange = onValueChange,
            centeredText = centeredText,
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TextFieldContent(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    value: String?,
    textStyle: TextStyle,
    alertState: PlatformInputAlertState,
    placeHolder: String?,
    keyboardOptions: KeyboardOptions,
    onValueChange: (String) -> Unit = {},
    centeredText: Boolean,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val currentValue = remember { mutableStateOf<String?>(value) } // value during editing
    if (!isFocused) {
        currentValue.value = value
    }
    val displayValue = if (isFocused) currentValue.value ?: "" else value ?: ""
    val textColor =
        if (isFocused) ThemeColor.SemanticColor.text_primary else alertState.textColor

    BasicTextField(
        modifier = modifier
            .focusRequester(focusRequester = focusRequester),
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
            Row(
                modifier = if (centeredText) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier
                }
                    .themeColor(background = ThemeColor.SemanticColor.transparent),
                horizontalArrangement = Arrangement.Center,
            ) {
                if (displayValue.isEmpty()) {
                    if (!(centeredText && isFocused)) {
                        Text(
                            text = placeHolder ?: "",
                            style = textStyle.themeColor(ThemeColor.SemanticColor.text_tertiary),
                            maxLines = 1,
                            textAlign = if (centeredText) TextAlign.Center else TextAlign.Start,
                        )
                    }
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
                colors = TextFieldDefaults.textFieldColors(
                    textColor = ThemeColor.SemanticColor.text_primary.color,
                    backgroundColor = ThemeColor.SemanticColor.transparent.color,
                ),
            )
        },
    )
}
