package exchange.dydx.trading.feature.market.marketlist.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.tabgroups.PlatformPillTextGroup
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxMarketAssetFilterView() {
    DydxThemedPreviewSurface {
        DydxMarketAssetFilterView.Content(Modifier, DydxMarketAssetFilterView.ViewState.preview)
    }
}

object DydxMarketAssetFilterView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val contents: List<String>,
        val onSelectionChanged: (Int) -> Unit,
        val selectedIndex: Int? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                contents = listOf("All", "Saved"),
                onSelectionChanged = { },
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketAssetFilterViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        var selectedIndex by remember { mutableIntStateOf(state.selectedIndex ?: 0) }

        Row(
            modifier = modifier
                .padding(vertical = ThemeShapes.VerticalPadding),
        ) {
            PlatformPillTextGroup(
                modifier = Modifier,
                items = state.contents,
                selectedItems = state.contents,
                currentSelection = state.selectedIndex,
                onSelectionChanged = { index ->
                    selectedIndex = index
                    state.onSelectionChanged(index)
                },
                itemStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                selectedItemStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_primary),
                scrollingEnabled = true,
            )
        }
    }
}
