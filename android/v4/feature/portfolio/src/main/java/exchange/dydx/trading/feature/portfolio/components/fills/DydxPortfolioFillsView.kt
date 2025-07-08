package exchange.dydx.trading.feature.portfolio.components.fills

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.portfolio.components.DydxPortfolioSelectorView
import exchange.dydx.trading.feature.portfolio.components.fills.DydxPortfolioFillsView.fillsListContent
import exchange.dydx.trading.feature.portfolio.components.placeholder.DydxPortfolioPlaceholderView
import exchange.dydx.trading.feature.shared.viewstate.SharedFillViewState

@Preview
@Composable
fun Preview_DydxPortfolioFillsView() {
    DydxThemedPreviewSurface {
        LazyColumn {
            fillsListContent(
                DydxPortfolioFillsView.ViewState.preview,
            )
        }
    }
}

object DydxPortfolioFillsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val fills: List<SharedFillViewState> = listOf(),
        val onFillTappedAction: (String) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                fills = listOf(
                    SharedFillViewState.preview,
                    SharedFillViewState.preview,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        Content(modifier, isFullScreen = false)
    }

    @Composable
    fun Content(modifier: Modifier, isFullScreen: Boolean) {
        val viewModel: DydxPortfolioFillsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        if (isFullScreen) {
            Column(
                modifier = modifier.fillMaxWidth()
                    .themeColor(ThemeColor.SemanticColor.layer_2),
            ) {
                DydxPortfolioSelectorView.Content(
                    modifier = Modifier
                        .height(72.dp)
                        .padding(horizontal = ThemeShapes.HorizontalPadding)
                        .fillMaxWidth(),
                )

                PlatformDivider()

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    fillsListContent(state)
                }
            }
        } else {
            LazyColumn(
                modifier = modifier,
            ) {
                fillsListContent(state)
            }
        }
    }

    fun LazyListScope.fillsListContent(state: ViewState?) {
        if (state == null) return

        if (state.fills.isEmpty()) {
            item(key = "placeholder") {
                DydxPortfolioPlaceholderView.Content(Modifier.padding(vertical = 0.dp))
            }
        } else {
            item(key = "header") {
                CreateHeader(Modifier, state)
            }

            items(items = state.fills, key = { it.id }) { fill ->
                if (fill === state.fills.first()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                DydxPortfolioFillItemView.Content(
                    modifier = Modifier
                        .clickable { state.onFillTappedAction(fill.id) },
                    state = fill,
                )

                if (fill !== state.fills.last()) {
                    PlatformDivider()
                }
            }
        }
    }

    @Composable
    private fun CreateHeader(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding * 2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                modifier = Modifier.width(80.dp),
                text = state.localizer.localize("APP.GENERAL.TIME"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Text(
                text = state.localizer.localize("APP.GENERAL.TYPE_AMOUNT"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = state.localizer.localize("APP.GENERAL.PRICE_FEE"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }
}
