package exchange.dydx.trading.feature.portfolio

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer
import exchange.dydx.trading.feature.portfolio.components.DydxPortfolioHeaderView
import exchange.dydx.trading.feature.portfolio.components.DydxPortfolioSelectorView
import exchange.dydx.trading.feature.portfolio.components.fills.DydxPortfolioFillsView
import exchange.dydx.trading.feature.portfolio.components.fills.DydxPortfolioFillsViewModel
import exchange.dydx.trading.feature.portfolio.components.orders.DydxPortfolioOrdersView
import exchange.dydx.trading.feature.portfolio.components.orders.DydxPortfolioOrdersViewModel
import exchange.dydx.trading.feature.portfolio.components.overview.DydxPortfolioChartView
import exchange.dydx.trading.feature.portfolio.components.overview.DydxPortfolioDetailsView
import exchange.dydx.trading.feature.portfolio.components.overview.DydxPortfolioSectionsView
import exchange.dydx.trading.feature.portfolio.components.positions.DydxPortfolioPositionsView
import exchange.dydx.trading.feature.portfolio.components.positions.DydxPortfolioPositionsViewModel
import exchange.dydx.trading.feature.shared.bottombar.DydxBottomBarScaffold

@Preview
@Composable
fun Preview_DydxPortfolioView() {
    DydxThemedPreviewSurface {
        DydxPortfolioView.Content(Modifier, DydxPortfolioView.ViewState.preview)
    }
}

object DydxPortfolioView : DydxComponent {
    enum class DisplayContent {
        Overview, Positions, Orders, Trades, Fees, Transfers, Payments;

        val stringKey: String
            get() = when (this) {
                Overview -> "APP.GENERAL.OVERVIEW"
                Positions -> "APP.TRADE.POSITIONS"
                Orders -> "APP.GENERAL.ORDERS"
                Trades -> "APP.GENERAL.TRADES"
                Fees -> "APP.GENERAL.FEES"
                Transfers -> "APP.GENERAL.TRANSFERS"
                Payments -> "APP.TRADE.FUNDING_PAYMENTS_SHORT"
            }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val displayContent: DisplayContent = DisplayContent.Overview,
        val tabSelection: DydxPortfolioSectionsView.Selection = DydxPortfolioSectionsView.Selection.Positions,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxPortfolioViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        DydxBottomBarScaffold(Modifier) {
            Content(it, state)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        val positionsViewModel: DydxPortfolioPositionsViewModel = hiltViewModel()
        val positionsViewState = positionsViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        val ordersViewModel: DydxPortfolioOrdersViewModel = hiltViewModel()
        val ordersViewState = ordersViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        val fillsViewModel: DydxPortfolioFillsViewModel = hiltViewModel()
        val fillsViewState = fillsViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        Column(
            modifier = modifier
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            CreateHeader(Modifier, state)

            LazyColumn(
                modifier = Modifier,
                state = listState,
            ) {
                item(key = "summary") {
                    DydxPortfolioChartView.Content(Modifier)

                    DydxPortfolioDetailsView.Content(Modifier)
                }

                stickyHeader(key = "sections") {
                    DydxPortfolioSectionsView.Content(
                        Modifier
                            .themeColor(ThemeColor.SemanticColor.layer_2)
                            .fillMaxWidth()
                            .padding(vertical = ThemeShapes.VerticalPadding * 3),
                    )
                }

                when (state.tabSelection) {
                    DydxPortfolioSectionsView.Selection.Positions -> {
                        DydxPortfolioPositionsView.ListContent(this, Modifier, positionsViewState)
                    }

                    DydxPortfolioSectionsView.Selection.Orders -> {
                        DydxPortfolioOrdersView.ListContent(this, Modifier, ordersViewState)
                    }

                    DydxPortfolioSectionsView.Selection.Trades -> {
                        DydxPortfolioFillsView.ListContent(this, Modifier, fillsViewState)
                    }

                    else -> {
                    }
                }
            }
        }
    }

    @Composable
    private fun CreateHeader(modifier: Modifier = Modifier, state: ViewState) {
        Row(
            modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(vertical = ThemeShapes.VerticalPadding)
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            DydxPortfolioSelectorView.Content(modifier)

            Spacer(modifier = Modifier.weight(1f))

            if (state.displayContent == DisplayContent.Overview || state.displayContent == DisplayContent.Transfers) {
                DydxPortfolioHeaderView.Content(modifier)
            }
        }
    }
}
