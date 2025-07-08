package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.menus.PlatformDropdownMenu
import exchange.dydx.platformui.components.menus.PlatformMenuItem
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_LabeledSelectionInput() {
    DydxThemedPreviewSurface {
        LabeledSelectionInput.Content(Modifier, LabeledSelectionInput.ViewState.preview)
    }
}

object LabeledSelectionInput {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val label: String? = null,
        val options: List<String> = listOf(),
        val selectedIndex: Int = 0,
        val onSelectionChanged: (Int) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                label = "Time in Force",
                options = listOf("Good Till Cancelled", "Immediate or Cancel", "Fill or Kill", "Good Till Time", "Good Till Date"),
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val expanded: MutableState<Boolean> = remember {
            mutableStateOf(false)
        }

        Row(
            modifier = modifier
                .clickable { expanded.value = !expanded.value }
                .padding(ThemeShapes.InputPaddingValues)
                .heightIn(min = ThemeShapes.InputHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (state.label != null) {
                    Text(
                        text = state.label,
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )
                }

                Text(
                    text = state.options.getOrNull(state.selectedIndex) ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                PlatformDropdownMenu(
                    expanded = expanded,
                    items = state.options.mapIndexed() { index, content ->
                        PlatformMenuItem(
                            text = content,
                            onClick = {
                                state.onSelectionChanged(index)
                                expanded.value = false
                            },
                        )
                    },
                    selectedIndex = state.selectedIndex,
                )
            }

            PlatformImage(
                icon = if (expanded.value) R.drawable.icon_triangle_up else R.drawable.icon_triangle_down,
                modifier = Modifier.width(10.dp),
            )
        }
    }
}
