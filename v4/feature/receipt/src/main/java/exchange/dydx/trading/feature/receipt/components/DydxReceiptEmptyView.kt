package exchange.dydx.trading.feature.receipt.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont

@Composable
fun DydxReceiptEmptyView() {
    Text(
        text = "-",
        style = TextStyle.dydxDefault
            .themeFont(fontSize = ThemeFont.FontSize.small)
            .themeColor(ThemeColor.SemanticColor.text_tertiary),
    )
}
