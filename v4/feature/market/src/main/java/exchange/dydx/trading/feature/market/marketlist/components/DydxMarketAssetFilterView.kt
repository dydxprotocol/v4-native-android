package exchange.dydx.trading.feature.market.marketlist.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformPillButton
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer

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
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            state.contents.forEachIndexed { index, item ->
                val buttonColor = if (selectedIndex == index) {
                    ThemeColor.SemanticColor.layer_0
                } else {
                    ThemeColor.SemanticColor.layer_5
                }
                PlatformPillButton(
                    modifier = Modifier.padding(end = 8.dp),
                    action = {
                        selectedIndex = index
                        state.onSelectionChanged(index)
                    },
                    backgroundColor = buttonColor,
                    enabled = selectedIndex != index,
                ) {
                    Text(
                        text = item,
                        style = if (selectedIndex == index) {
                            TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.small)
                                .themeColor(ThemeColor.SemanticColor.text_primary)
                        } else {
                            TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.small)
                                .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        },
                    )
                }
            }
        }
    }
}
