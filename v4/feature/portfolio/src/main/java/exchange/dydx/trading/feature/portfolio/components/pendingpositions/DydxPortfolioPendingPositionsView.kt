package exchange.dydx.trading.feature.portfolio.components.pendingpositions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.navigation.PortfolioRoutes.positions

@Preview
@Composable
fun Preview_DydxPortfolioPendingPositionsView() {
    DydxThemedPreviewSurface {
        DydxPortfolioPendingPositionsView.Content(
            Modifier,
            DydxPortfolioPendingPositionsView.ViewState.preview,
        )
    }
}

object DydxPortfolioPendingPositionsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val positions: List<DydxPortfolioPendingPositionItemView.ViewState> = listOf(),

    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                positions = listOf(
                    DydxPortfolioPendingPositionItemView.ViewState.preview,
                    DydxPortfolioPendingPositionItemView.ViewState.preview,
                    DydxPortfolioPendingPositionItemView.ViewState.preview,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxPortfolioPendingPositionsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null || state.positions.isEmpty()) {
            return
        }

        Column(
            modifier = modifier
                .padding(ThemeShapes.HorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = state.localizer.localize("APP.TRADE.UNOPENED_ISOLATED_POSITIONS"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.large)
                    .themeColor(ThemeColor.SemanticColor.text_primary),
            )

            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val itemsPerRow = 2
                for (i in 0 until positions.count() / itemsPerRow) {
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        for (j in 0 until itemsPerRow) {
                            if (i * itemsPerRow + j >= state.positions.count()) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val position = state.positions[i * itemsPerRow + j]
                                DydxPortfolioPendingPositionItemView.Content(
                                    Modifier.weight(1f),
                                    position,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun LazyListScope.pendingPositionsListContent(state: ViewState?) {
        if (state == null) return

        item {
            Content(Modifier, state)
        }
    }
}
