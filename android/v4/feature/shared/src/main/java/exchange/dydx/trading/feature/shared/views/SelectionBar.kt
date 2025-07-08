package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.tabgroups.PlatformTabGroup
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer

@Preview
@Composable
fun Preview_SelectionBar() {
    DydxThemedPreviewSurface {
        SelectionBar.Content(Modifier, SelectionBar.ViewState.preview)
    }
}

object SelectionBar {
    enum class Style {
        Large, Medium;

        val fontSize: ThemeFont.FontSize
            get() {
                return when (this) {
                    Large -> ThemeFont.FontSize.extra
                    Medium -> ThemeFont.FontSize.large
                }
            }

        val fontType: ThemeFont.FontType
            get() {
                return when (this) {
                    Large -> ThemeFont.FontType.plus
                    Medium -> ThemeFont.FontType.book
                }
            }
    }

    data class Item(
        val text: String?,
    ) {
        companion object {
            val preview = Item(
                text = "1.0M",
            )
        }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val items: List<Item> = emptyList(),
        val currentSelection: Int? = null,
        val onSelectionChanged: (Int) -> Unit = {},
        val style: Style = Style.Large,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                items = listOf(
                    Item.preview,
                    Item.preview,
                    Item.preview,
                ),
                currentSelection = 1,
            )
        }
    }

    @Composable
    fun Content(
        modifier: Modifier,
        state: ViewState?,
    ) {
        if (state == null) {
            return
        }

        PlatformTabGroup(
            modifier = modifier.fillMaxWidth(),
            scrollingEnabled = false,
            items = state.items.map {
                { modifier ->
                    ItemView(
                        modifier = modifier,
                        item = it,
                        isSelected = false,
                        fontSize = state.style.fontSize,
                        fontType = state.style.fontType,
                    )
                }
            },
            selectedItems = state.items.map {
                { modifier ->
                    ItemView(
                        modifier = modifier,
                        item = it,
                        isSelected = true,
                        fontSize = state.style.fontSize,
                        fontType = state.style.fontType,
                    )
                }
            },
            equalWeight = true,
            currentSelection = state.currentSelection,
            onSelectionChanged = state.onSelectionChanged,
        )
    }

    @Composable
    private fun ItemView(
        modifier: Modifier,
        item: Item,
        isSelected: Boolean,
        fontSize: ThemeFont.FontSize,
        fontType: ThemeFont.FontType,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = item.text ?: "",
                modifier = modifier
                    .padding(vertical = 16.dp),
                style = TextStyle.dydxDefault
                    .themeColor(
                        if (isSelected) {
                            ThemeColor.SemanticColor.text_primary
                        } else {
                            ThemeColor.SemanticColor.text_tertiary
                        },
                    )
                    .themeFont(fontSize = fontSize, fontType = fontType),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RectangleShape)
                    .themeColor(
                        if (isSelected) {
                            ThemeColor.SemanticColor.color_purple
                        } else {
                            ThemeColor.SemanticColor.transparent
                        },
                    ),
            )
        }
    }
}
