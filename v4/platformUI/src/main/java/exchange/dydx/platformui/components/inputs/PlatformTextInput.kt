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
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
    label: @Composable (() -> Unit)? = null,
    value: String? = null,
    textStyle: TextStyle =
        TextStyle.dydxDefault
            .themeFont(
                fontSize = ThemeFont.FontSize.medium,
                fontType = ThemeFont.FontType.number,
            ),
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
            val currentValue = remember { mutableStateOf<String?>(null) }
            val displayValue = if (isFocused) currentValue.value ?: "" else value ?: ""

            BasicTextField(
                modifier = Modifier,
                value = displayValue,
                onValueChange = {
                    currentValue.value = it
                    onValueChange(it)
                },
                singleLine = true,
                keyboardOptions = keyboardOptions,
                interactionSource = interactionSource,
                textStyle = textStyle
                    .themeColor(ThemeColor.SemanticColor.text_primary),
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

//        TextField(
//            modifier = Modifier,
//            value = displayValue,
//            onValueChange = {
//                currentValue.value = it
//                onValueChange(it)
//            },
//            //  label = label,
//            placeholder = {
//                Text(
//                    text = placeHolder ?: "",
//                    style = TextStyle.dydxDefault
//                        .themeFont(fontSize = ThemeFont.FontSize.medium)
//                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
//                )
//            },
//            interactionSource = interactionSource,
//            singleLine = true,
//            colors = inputFieldColors,
//            textStyle = TextStyle.dydxDefault
//                .themeFont(fontSize = ThemeFont.FontSize.medium)
//        )
        }
    }
}

@Composable
fun PlatformTextInput(
    labelText: String? = null,
    value: String? = null,
    placeHolder: String? = null,
    onValueChange: (String) -> Unit = {},
) {
    PlatformTextInput(
        label = if (labelText?.isNotEmpty() == true) { {
            Text(
                text = labelText ?: "",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        } } else {
            null
        },
        value = value,
        placeHolder = placeHolder,
        onValueChange = onValueChange,
    )
}

private val inputFieldColors: TextFieldColors
    @Composable
    get() = TextFieldDefaults.textFieldColors(
        textColor = ThemeColor.SemanticColor.text_primary.color,
        disabledTextColor = ThemeColor.SemanticColor.text_tertiary.color,
        backgroundColor = ThemeColor.SemanticColor.transparent.color,
        focusedIndicatorColor = ThemeColor.SemanticColor.transparent.color,
        unfocusedIndicatorColor = ThemeColor.SemanticColor.transparent.color,
        errorIndicatorColor = ThemeColor.SemanticColor.transparent.color,
        cursorColor = ThemeColor.SemanticColor.text_primary.color,
        errorCursorColor = ThemeColor.SemanticColor.color_yellow.color,
        errorLabelColor = ThemeColor.SemanticColor.color_yellow.color,
        errorLeadingIconColor = ThemeColor.SemanticColor.color_yellow.color,
        errorTrailingIconColor = ThemeColor.SemanticColor.color_yellow.color,
    )
