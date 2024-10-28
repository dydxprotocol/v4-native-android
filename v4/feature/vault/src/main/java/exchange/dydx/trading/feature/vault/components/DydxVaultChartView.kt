package exchange.dydx.trading.feature.vault.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformPillItem
import exchange.dydx.platformui.components.menus.PlatformDropdownMenu
import exchange.dydx.platformui.components.menus.PlatformMenuItem
import exchange.dydx.platformui.components.tabgroups.PlatformPillTextGroup
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.SparklineView

@Preview
@Composable
fun Preview_DydxVaultChartView() {
    DydxThemedPreviewSurface {
        DydxVaultChartView.Content(Modifier, DydxVaultChartView.ViewState.preview)
    }
}

object DydxVaultChartView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val typeTitles: List<String>? = null,
        val typeIndex: Int? = null,
        val onTypeChanged: (Int) -> Unit = {},
        val resolutionTitles: List<String>? = null,
        val resolutionIndex: Int? = null,
        val onResolutionChanged: (Int) -> Unit = {},
        val sparkline: SparklineView.ViewState? = null
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                typeTitles = listOf("Value P&L", "Value Equity"),
                typeIndex = 0,
                resolutionTitles = listOf("1d", "7d", "30d"),
                resolutionIndex = 0,
                sparkline = SparklineView.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultChartViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            TopSelectorContent(
                modifier = Modifier
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(top = 16.dp, bottom = 8.dp),
                state = state,
            )
            SparklineView.Content(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(vertical = 0.dp)
                    .padding(horizontal = 0.dp),
                state = state.sparkline,
            )
        }
    }

    @Composable
    private fun TopSelectorContent(modifier: Modifier, state: ViewState) {
        val expanded: MutableState<Boolean> = remember {
            mutableStateOf(false)
        }

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.weight(1f),
            ) {
                PlatformPillTextGroup(
                    modifier = Modifier,
                    items = state.typeTitles ?: emptyList(),
                    selectedItems = state.typeTitles ?: emptyList(),
                    currentSelection = state.typeIndex ?: 0,
                    onSelectionChanged = { index ->
                        state.onTypeChanged(index)
                    },
                    itemStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.mini)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    selectedItemStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.mini)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )
            }

            Box {
                PlatformPillItem(
                    modifier = Modifier
                        .clickable { expanded.value = !expanded.value },
                    backgroundColor = ThemeColor.SemanticColor.layer_5,
                ) {
                    Text(
                        text = state.resolutionIndex?.let { state.resolutionTitles?.get(it) } ?: "",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )
                }

                PlatformDropdownMenu(
                    modifier = Modifier,
                    expanded = expanded,
                    items = (state.resolutionTitles ?: listOf()).mapIndexed { index, content ->
                        PlatformMenuItem(
                            text = content,
                            onClick = {
                                state.onResolutionChanged(index)
                                expanded.value = false
                            },
                        )
                    },
                    selectedIndex = state.resolutionIndex,
                )
            }
        }
    }
}
