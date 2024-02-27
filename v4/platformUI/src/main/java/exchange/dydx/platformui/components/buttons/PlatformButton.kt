package exchange.dydx.platformui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import kotlinx.serialization.json.JsonNull.content

enum class PlatformButtonState {
    Primary, Secondary, Disabled, Destructive,
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
    trailingContent: @Composable (() -> Unit)? = null,
    action: () -> Unit,
) {
    val borderColor = when (state) {
        PlatformButtonState.Primary -> ThemeColor.SemanticColor.color_purple.color
        PlatformButtonState.Secondary -> ThemeColor.SemanticColor.layer_5.color
        PlatformButtonState.Disabled -> ThemeColor.SemanticColor.layer_6.color
        PlatformButtonState.Destructive -> ThemeColor.SemanticColor.color_faded_red.color
    }
    val backgroundColor = when (state) {
        PlatformButtonState.Primary -> ThemeColor.SemanticColor.color_purple.color
        PlatformButtonState.Secondary -> ThemeColor.SemanticColor.layer_5.color
        PlatformButtonState.Disabled -> ThemeColor.SemanticColor.layer_2.color
        PlatformButtonState.Destructive -> ThemeColor.SemanticColor.layer_4.color
    }
    val enabled = when (state) {
        PlatformButtonState.Primary -> true
        PlatformButtonState.Secondary -> true
        PlatformButtonState.Disabled -> false
        PlatformButtonState.Destructive -> true
    }
    val textColor = when (state) {
        PlatformButtonState.Primary -> ThemeColor.SemanticColor.color_white
        PlatformButtonState.Secondary -> ThemeColor.SemanticColor.text_primary
        PlatformButtonState.Disabled -> ThemeColor.SemanticColor.text_tertiary
        PlatformButtonState.Destructive -> ThemeColor.SemanticColor.color_red
    }

    OutlinedButton(
        modifier = modifier.height(52.dp),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(size = 8.dp),
        colors = ButtonDefaults
            .outlinedButtonColors(backgroundColor),
        enabled = enabled,
        onClick = action,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier,
        ) {
            if (text != null) {
                Text(
                    style = TextStyle.dydxDefault.themeFont(fontSize = fontSize)
                        .themeColor(foreground = textColor),
                    text = text,
                )
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}
