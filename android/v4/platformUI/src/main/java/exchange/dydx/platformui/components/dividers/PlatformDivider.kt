package exchange.dydx.platformui.components.dividers

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color

@Composable
fun PlatformDivider(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier,
        startIndent = 0.dp,
        thickness = 1.dp,
        color = ThemeColor.SemanticColor.layer_6.color,
    )
}

@Composable
fun PlatformVerticalDivider(modifier: Modifier = Modifier) {
    PlatformDivider(
        modifier = modifier
            .fillMaxHeight() // fill the max height
            .width(1.dp),
    )
}
