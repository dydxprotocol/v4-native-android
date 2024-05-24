package exchange.dydx.trading.feature.portfolio.components.positions

import androidx.compose.foundation.layout.Box
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
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer
import exchange.dydx.trading.feature.portfolio.components.DydxPortfolioSelectorView
import exchange.dydx.trading.feature.portfolio.components.placeholder.DydxPortfolioPlaceholderView
import exchange.dydx.trading.feature.portfolio.components.positions.DydxPortfolioPositionsView.positionsListContent
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketPositionViewState

@Preview
@Composable
fun Preview_DydxPortfolioPositionsView() {
    DydxThemedPreviewSurface {
        LazyColumn {
            positionsListContent(DydxPortfolioPositionsView.ViewState.preview)
        }
    }
}

object DydxPortfolioPositionsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val positions: List<SharedMarketPositionViewState> = listOf(),
        val isIsolatedMarketEnabled: Boolean,
        val onPositionTapAction: (SharedMarketPositionViewState) -> Unit = {},
        val onModifyMarginAction: (SharedMarketPositionViewState) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                positions = listOf(
                    SharedMarketPositionViewState.preview,
                    SharedMarketPositionViewState.preview,
                ),
                isIsolatedMarketEnabled = false,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        Content(modifier, isFullScreen = false)
    }

    @Composable
    fun Content(modifier: Modifier, isFullScreen: Boolean) {
        val viewModel: DydxPortfolioPositionsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        if (isFullScreen) {
            Column(
                modifier = modifier.fillMaxWidth(),
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
                    this.positionsListContent(state)
                }
            }
        } else {
            LazyColumn(
                modifier = modifier,
            ) {
                positionsListContent(state)
            }
        }
    }

    fun LazyListScope.positionsListContent(state: ViewState?) {
        if (state == null) return

        if (state.positions.isEmpty()) {
            item(key = "placeholder") {
                DydxPortfolioPlaceholderView.Content(Modifier.padding(vertical = 0.dp))

                CreateFooter(Modifier, state)
            }
        } else {
            if (!state.isIsolatedMarketEnabled) {
                item(key = "header") {
                    CreateHeader(Modifier, state)
                }
            }

            items(items = state.positions, key = { it.id }) { position ->
//                if (!state.isIsolatedMarketEnabled && position === state.positions.first()) {
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
                DydxPortfolioPositionItemView.Content(
                    modifier = Modifier,
                    localizer = state.localizer,
                    position = position,
                    isIsolatedMarketEnabled = state.isIsolatedMarketEnabled,
                    onTapAction = state.onPositionTapAction,
                    onModifyMarginAction = state.onModifyMarginAction,
                )

//              Spacer(modifier = Modifier.height(10.dp))
            }

            item(key = "footer") {
                CreateFooter(Modifier, state)
            }
        }
    }

    @Composable
    private fun CreateFooter(modifier: Modifier, state: ViewState) {
        if (!state.isIsolatedMarketEnabled) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier.fillMaxWidth()
                    .padding(vertical = 24.dp),
            ) {
                Text(
                    text = state.localizer.localize("APP.GENERAL.ISOLATED_POSITIONS_COMING_SOON"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding * 2),
                )
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
        ) {
            Text(
                text = state.localizer.localize("APP.GENERAL.DETAILS"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = state.localizer.localize("APP.GENERAL.INDEX_ENTRY"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Row(
                modifier = Modifier.width(80.dp),
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = state.localizer.localize("APP.GENERAL.PROFIT_AND_LOSS"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }
        }
    }
}
