package exchange.dydx.platformui.components.changes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont

@Composable
fun PlatformAmountChange(
    modifier: Modifier = Modifier,
    before: (@Composable () -> Unit)? = null,
    after: (@Composable () -> Unit)? = null,
    direction: PlatformDirection = PlatformDirection.None,
    textStyle: TextStyle = TextStyle.dydxDefault
        .themeColor(ThemeColor.SemanticColor.text_tertiary)
        .themeFont(fontSize = ThemeFont.FontSize.small)
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        if (before != null && after != null) {
            before()
            Spacer(modifier = Modifier.size(8.dp))
            PlatformDirectionArrow(direction = direction, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.size(8.dp))
            after()
        } else if (before != null) {
            before()
        } else if (after != null) {
            after()
        } else {
            Text(
                text = "-",
                style = textStyle,
            )
        }
    }
}
