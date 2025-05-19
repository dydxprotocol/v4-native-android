package exchange.dydx.trading.feature.market.marketlist.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformRoundIcon
import exchange.dydx.platformui.components.menus.PlatformDropdownMenu
import exchange.dydx.platformui.components.menus.PlatformMenuItem
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxMarketAssetSortView() {
    DydxThemedPreviewSurface {
        DydxMarketAssetSortView.Content(Modifier, DydxMarketAssetSortView.ViewState.preview)
    }
}

object DydxMarketAssetSortView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val contents: List<String>,
        val onSelectionChanged: (Int) -> Unit,
        val selectedIndex: Int? = null,
    ) {
        val selectedFilter: String?
            get() = selectedIndex?.let { contents.getOrNull(it) }

        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                contents = listOf("Price", "Name", "Gainers", "Losers"),
                onSelectionChanged = { },
                selectedIndex = 0,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketAssetSortViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
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
            modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
            ) {
                Text(
                    text = state.localizer.localize("APP.GENERAL.MARKETS"),
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.mini,
                        )
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clickable { expanded.value = !expanded.value },
                ) {
                    Text(
                        text = state.selectedFilter ?: "",
                        style = TextStyle.dydxDefault
                            .themeFont(
                                fontSize = ThemeFont.FontSize.mini,
                            )
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )

                    PlatformRoundIcon(
                        icon = R.drawable.icon_sort,
                        size = 25.dp,
                        iconSize = 14.dp,
                        iconTint = ThemeColor.SemanticColor.text_tertiary,
                    )
                }

                PlatformDropdownMenu(
                    expanded = expanded,
                    items = state.contents.mapIndexed() { index, content ->
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
        }
    }
}
