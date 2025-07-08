package exchange.dydx.platformui.components.tabgroups

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont

@Composable
fun PlatformTextTabGroup(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedItems: List<String>,
    itemStyle: TextStyle = TextStyle.dydxDefault
        .themeColor(ThemeColor.SemanticColor.text_tertiary)
        .themeFont(fontType = ThemeFont.FontType.plus, fontSize = ThemeFont.FontSize.extra),
    selectedItemStyle: TextStyle = TextStyle.dydxDefault
        .themeColor(ThemeColor.SemanticColor.text_primary)
        .themeFont(fontType = ThemeFont.FontType.plus, fontSize = ThemeFont.FontSize.extra),
    currentSelection: Int? = null,
    onSelectionChanged: (Int) -> Unit = {},
) {
    PlatformTabGroup(
        modifier = modifier,
        items = items.map { item ->
            { modifier ->
                Text(
                    text = item,
                    modifier = modifier,
                    style = itemStyle,
                )
            }
        },
        selectedItems = selectedItems.map { item ->
            { modifier ->
                Text(
                    text = item,
                    modifier = modifier,
                    style = selectedItemStyle,
                )
            }
        },
        currentSelection = currentSelection,
        onSelectionChanged = onSelectionChanged,
    )
}
