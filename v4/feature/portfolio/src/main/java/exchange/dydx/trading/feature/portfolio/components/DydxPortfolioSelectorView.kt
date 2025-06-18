package exchange.dydx.trading.feature.portfolio.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import exchange.dydx.trading.feature.portfolio.DydxPortfolioView
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxPortfolioSelectorView() {
    DydxThemedPreviewSurface {
        DydxPortfolioSelectorView.Content(Modifier, DydxPortfolioSelectorView.ViewState.preview)
    }
}

object DydxPortfolioSelectorView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val contents: List<DydxPortfolioView.DisplayContent> = listOf(
            DydxPortfolioView.DisplayContent.Overview,
            DydxPortfolioView.DisplayContent.Positions,
            DydxPortfolioView.DisplayContent.Orders,
            DydxPortfolioView.DisplayContent.Funding,
            // DydxPortfolioView.DisplayContent.Fees,
            DydxPortfolioView.DisplayContent.Trades,
            DydxPortfolioView.DisplayContent.Transfers,
        ),
        val onSelectionChanged: (DydxPortfolioView.DisplayContent) -> Unit = {},
        val currentContent: DydxPortfolioView.DisplayContent = DydxPortfolioView.DisplayContent.Overview,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxPortfolioSelectorViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        val expanded: MutableState<Boolean> = remember {
            mutableStateOf(false)
        }

        Box(
            modifier = modifier,
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clickable { expanded.value = !expanded.value },
            ) {
                Text(
                    text = state.localizer.localize(state.currentContent.stringKey),
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.extra,
                            fontType = ThemeFont.FontType.plus,
                        )
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                PlatformRoundIcon(
                    icon = R.drawable.icon_arrow_down,
                    size = 28.dp,
                    iconSize = 15.dp,
                    iconTint = ThemeColor.SemanticColor.text_tertiary,
                    borderColor = ThemeColor.SemanticColor.layer_6,
                )
            }

            PlatformDropdownMenu(
                expanded = expanded,
                items = state.contents.mapIndexed() { index, content ->
                    PlatformMenuItem(
                        text = state.localizer.localize(content.stringKey),
                        subText = state.localizer.localize(content.subTextStringKey),
                        onClick = {
                            expanded.value = false
                            state.onSelectionChanged(content)
                        },
                    )
                },
                selectedIndex = state.contents.indexOf(state.currentContent),
            )
        }
    }
}
