package exchange.dydx.trading.feature.portfolio.components.positions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.portfolio.components.DydxPortfolioSelectorView
import exchange.dydx.trading.feature.portfolio.components.pendingpositions.DydxPortfolioPendingPositionsView.pendingPositionsListContent
import exchange.dydx.trading.feature.portfolio.components.pendingpositions.DydxPortfolioPendingPositionsViewModel
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
        val onboarded: Boolean,
        val onPositionTapAction: (SharedMarketPositionViewState) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                positions = listOf(
                    SharedMarketPositionViewState.preview,
                    SharedMarketPositionViewState.preview,
                ),
                onboarded = true,
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

        val pendingPositionsViewModel: DydxPortfolioPendingPositionsViewModel = hiltViewModel()
        val pendingPositionsViewState = pendingPositionsViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

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
                    this.positionsListContent(state)
                    this.pendingPositionsListContent(pendingPositionsViewState)
                }
            }
        } else {
            LazyColumn(
                modifier = modifier,
            ) {
                positionsListContent(state)
                pendingPositionsListContent(pendingPositionsViewState)
            }
        }
    }

    fun LazyListScope.positionsListContent(state: ViewState?) {
        if (state == null) return

        if (state.positions.isEmpty()) {
            item(key = "placeholder") {
                DydxPortfolioPlaceholderView.Content(Modifier.padding(vertical = 0.dp))
            }
        } else {
            items(items = state.positions, key = { it.id }) { position ->
                DydxPortfolioPositionItemView.Content(
                    modifier = Modifier,
                    localizer = state.localizer,
                    position = position,
                    onTapAction = state.onPositionTapAction,
                )
            }
        }
    }
}
