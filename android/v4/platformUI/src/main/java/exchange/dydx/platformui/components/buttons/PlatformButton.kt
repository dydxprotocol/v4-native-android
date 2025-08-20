package exchange.dydx.platformui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.components.textgroups.PlatformAutoSizingText
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont

sealed class PlatformButtonState(
    open val borderColor: ThemeColor.SemanticColor,
    open val backgroundColor: ThemeColor.SemanticColor,
    open val enabled: Boolean,
    open val textColor: ThemeColor.SemanticColor
) {
    data object Primary : PlatformButtonState(
        borderColor = ThemeColor.SemanticColor.color_purple,
        backgroundColor = ThemeColor.SemanticColor.color_purple,
        enabled = true,
        textColor = ThemeColor.SemanticColor.color_white,
    )

    data object Secondary : PlatformButtonState(
        borderColor = ThemeColor.SemanticColor.layer_6,
        backgroundColor = ThemeColor.SemanticColor.layer_4,
        enabled = true,
        textColor = ThemeColor.SemanticColor.text_primary,
    )

    data object Disabled : PlatformButtonState(
        borderColor = ThemeColor.SemanticColor.layer_6,
        backgroundColor = ThemeColor.SemanticColor.layer_2,
        enabled = false,
        textColor = ThemeColor.SemanticColor.text_tertiary,
    )

    data object Destructive : PlatformButtonState(
        borderColor = ThemeColor.SemanticColor.color_faded_red,
        backgroundColor = ThemeColor.SemanticColor.layer_4,
        enabled = true,
        textColor = ThemeColor.SemanticColor.color_red,
    )

    data class Custom(
        override val borderColor: ThemeColor.SemanticColor,
        override val backgroundColor: ThemeColor.SemanticColor,
        override val enabled: Boolean,
        override val textColor: ThemeColor.SemanticColor
    ) : PlatformButtonState(borderColor, backgroundColor, enabled, textColor)
}

@Composable
fun PlatformButton(
    modifier: Modifier = Modifier,
    action: () -> Unit,
    backgroundColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.layer_5,
    borderColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.layer_6,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable () -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        border = BorderStroke(1.dp, borderColor.color),
        shape = RoundedCornerShape(size = 8.dp),
        colors = ButtonDefaults
            .outlinedButtonColors(backgroundColor.color),
        enabled = enabled,
        contentPadding = contentPadding,
        onClick = action,
    ) {
        content()
    }
}

@Composable
fun PlatformButton(
    modifier: Modifier = Modifier,
    state: PlatformButtonState = PlatformButtonState.Primary,
    text: String?,
    fontSize: ThemeFont.FontSize = ThemeFont.FontSize.medium,
    cornerRadius: Dp = 8.dp,
    fitText: Boolean = false,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    action: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier.defaultMinSize(minHeight = 52.dp),
        border = BorderStroke(1.dp, state.borderColor.color),
        shape = RoundedCornerShape(size = cornerRadius),
        colors = ButtonDefaults
            .outlinedButtonColors(state.backgroundColor.color),
        enabled = state.enabled,
        onClick = action,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (leadingContent != null) {
                leadingContent()
            }
            if (text != null) {
                if (fitText) {
                    PlatformAutoSizingText(
                        textStyle = TextStyle.dydxDefault.themeFont(fontSize = fontSize)
                            .themeColor(foreground = state.textColor),
                        text = text,
                    )
                } else {
                    Text(
                        text = text,
                        style = TextStyle.dydxDefault.themeFont(fontSize = fontSize)
                            .themeColor(foreground = state.textColor),
                    )
                }
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}
