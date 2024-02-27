package exchange.dydx.platformui.components.menus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont

@Composable
fun PlatformDropdownMenu(
    expanded: MutableState<Boolean>,
    onDismissRequest: () -> Unit = { expanded.value = false },
    modifier: Modifier = Modifier,
    items: List<PlatformMenuItem>,
    selectedIndex: Int? = null,
) {
    MaterialTheme(
        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(8.dp)),
    ) {
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = onDismissRequest,
            modifier = modifier
                .themeColor(ThemeColor.SemanticColor.transparent)
                .background(color = ThemeColor.SemanticColor.layer_4.color, shape = RoundedCornerShape(8.dp))
                .border(width = 1.dp, color = ThemeColor.SemanticColor.layer_6.color, shape = RoundedCornerShape(8.dp)),
        ) {
            items.forEachIndexed() { index, item ->
                DropdownMenuItem(
                    modifier = Modifier
                        .background(
                            color = if (index == selectedIndex) {
                                ThemeColor.SemanticColor.layer_3.color
                            } else {
                                ThemeColor.SemanticColor.layer_4.color
                            },
                        ),
                    text = {
                        Text(
                            text = item.text,
                            style = TextStyle.dydxDefault
                                .themeFont(
                                    fontSize = ThemeFont.FontSize.mini,
                                )
                                .themeColor(ThemeColor.SemanticColor.text_secondary),
                        )
                    },
                    onClick = {
                        expanded.value = false
                        item.onClick()
                    },
                    trailingIcon = {
                        if (index == selectedIndex) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "...",
                                tint = ThemeColor.SemanticColor.text_secondary.color,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    },

                )
                if (index < items.size - 1) {
                    PlatformDivider()
                }
            }
        }
    }
}

data class PlatformMenuItem(
    val text: String,
    val onClick: () -> Unit,
)
