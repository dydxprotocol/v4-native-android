package exchange.dydx.platformui.components.inputs

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont

@Composable
fun PlatformSwitchInput(
    modifier: Modifier = Modifier,
    label: String? = null,
    textStyle: TextStyle = TextStyle.dydxDefault
        .themeColor(ThemeColor.SemanticColor.text_tertiary)
        .themeFont(fontSize = ThemeFont.FontSize.small),
    value: Boolean? = null,
    onValueChange: (Boolean) -> Unit = {},
    canEdit: Boolean = true,
) {
    val alpha = if (canEdit) 1f else 0.4f
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (label != null) {
            Text(
                modifier = Modifier.weight(1f).alpha(alpha),
                text = label,
                style = textStyle,
            )
        }

        Switch(
            modifier = Modifier.alpha(alpha),
            checked = value ?: false,
            onCheckedChange = onValueChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ThemeColor.SemanticColor.color_purple.color,
                checkedTrackColor = ThemeColor.SemanticColor.layer_6.color,
                uncheckedThumbColor = ThemeColor.SemanticColor.text_primary.color,
                uncheckedTrackColor = ThemeColor.SemanticColor.layer_6.color,
            ),
        )
    }
}
