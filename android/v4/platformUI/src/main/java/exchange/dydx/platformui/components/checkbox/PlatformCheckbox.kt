package exchange.dydx.platformui.components.checkbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.components.icons.PlatformSelectedIcon
import exchange.dydx.platformui.components.icons.PlatformUnselectedIcon
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont

@Composable
fun PlatformCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    textStyle: TextStyle = TextStyle.dydxDefault
        .themeColor(ThemeColor.SemanticColor.text_tertiary)
        .themeFont(fontSize = ThemeFont.FontSize.small),
    text: AnnotatedString,
    linkAction: ((String) -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    Row(
        modifier = modifier
            .clickable {
                onCheckedChange(!checked)
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (checked) {
            PlatformSelectedIcon(size = 24.dp)
        } else {
            PlatformUnselectedIcon(size = 24.dp)
        }

        ClickableText(
            modifier = modifier,
            text = text,
            style = textStyle,
            onClick = {
                val annotation = text.getStringAnnotations(start = it, end = it)
                    .firstOrNull()
                if (annotation != null) {
                    linkAction?.invoke(annotation.item)
                } else {
                    onCheckedChange(!checked)
                }
            },
        )
    }
}
