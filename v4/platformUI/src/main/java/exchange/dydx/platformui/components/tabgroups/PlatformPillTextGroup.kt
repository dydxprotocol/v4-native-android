package exchange.dydx.platformui.components.tabgroups

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.components.buttons.PlatformPillItem
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont

@Composable
fun PlatformPillTextGroup(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedItems: List<String>,
    itemStyle: TextStyle = TextStyle.dydxDefault
        .themeColor(ThemeColor.SemanticColor.text_secondary)
        .themeFont(fontType = ThemeFont.FontType.plus, fontSize = ThemeFont.FontSize.extra),
    selectedItemStyle: TextStyle = TextStyle.dydxDefault
        .themeColor(ThemeColor.SemanticColor.text_primary)
        .themeFont(fontType = ThemeFont.FontType.plus, fontSize = ThemeFont.FontSize.extra),
    padding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
    currentSelection: Int? = null,
    scrollingEnabled: Boolean = false,
    onSelectionChanged: (Int) -> Unit = {},
) {
    PlatformTabGroup(
        modifier = modifier,
        items = items.map { item ->
            { modifier ->
                PlatformPillItem(
                    modifier = modifier,
                    backgroundColor = ThemeColor.SemanticColor.layer_5,
                    padding = padding,
                ) {
                    Text(
                        text = item,
                        modifier = Modifier,
                        style = itemStyle,
                    )
                }
            }
        },
        selectedItems = selectedItems.map { item ->
            { modifier ->
                PlatformPillItem(
                    modifier = modifier,
                    backgroundColor = ThemeColor.SemanticColor.layer_2,
                    padding = padding,
                ) {
                    Text(
                        text = item,
                        modifier = Modifier,
                        style = selectedItemStyle,
                    )
                }
            }
        },
        currentSelection = currentSelection,
        onSelectionChanged = onSelectionChanged,
        scrollingEnabled = scrollingEnabled,
    )
}
