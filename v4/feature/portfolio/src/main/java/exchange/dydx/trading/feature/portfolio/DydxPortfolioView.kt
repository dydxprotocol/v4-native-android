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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.compose.PlatformRememberLazyListState
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.portfolio.components.DydxPortfolioHeaderView
import exchange.dydx.trading.feature.portfolio.components.DydxPortfolioSelectorView
import exchange.dydx.trading.feature.portfolio.components.fills.DydxPortfolioFillsView.fillsListContent
import exchange.dydx.trading.feature.portfolio.components.fills.DydxPortfolioFillsViewModel
import exchange.dydx.trading.feature.portfolio.components.fundings.DydxPortfolioFundingsView.fundingListContent
import exchange.dydx.trading.feature.portfolio.components.fundings.DydxPortfolioFundingsViewModel
import exchange.dydx.trading.feature.portfolio.components.orders.DydxPortfolioOrdersView.ordersListContent
import exchange.dydx.trading.feature.portfolio.components.orders.DydxPortfolioOrdersViewModel
import exchange.dydx.trading.feature.portfolio.components.overview.DydxPortfolioChartView
import exchange.dydx.trading.feature.portfolio.components.overview.DydxPortfolioDetailsView
import exchange.dydx.trading.feature.portfolio.components.overview.DydxPortfolioSectionsView
import exchange.dydx.trading.feature.portfolio.components.pendingpositions.DydxPortfolioPendingPositionsView.pendingPositionsListContent
import exchange.dydx.trading.feature.portfolio.components.pendingpositions.DydxPortfolioPendingPositionsViewModel
import exchange.dydx.trading.feature.portfolio.components.positions.DydxPortfolioPositionsView.positionsListContent
import exchange.dydx.trading.feature.portfolio.components.positions.DydxPortfolioPositionsViewModel
import exchange.dydx.trading.feature.portfolio.components.vault.DydxPortfolioVaultView
import exchange.dydx.trading.feature.shared.apprating.AppRatingDialog
import exchange.dydx.trading.feature.shared.apprating.AppRatingDialogScaffold
import exchange.dydx.trading.feature.shared.bottombar.DydxBottomBarScaffold
import kotlinx.coroutines.flow.MutableStateFlow

@Preview
@Composable
fun Preview_DydxPortfolioView() {
    DydxThemedPreviewSurface {
        DydxPortfolioView.Content(Modifier, DydxPortfolioView.ViewState.preview)
    }
}

object DydxPortfolioView : DydxComponent {
    enum class DisplayContent {
        Overview, Positions, Orders, Trades, Fees, Transfers, Funding;

        val stringKey: String
            get() = when (this) {
                Overview -> "APP.GENERAL.OVERVIEW"
                Positions -> "APP.TRADE.POSITIONS"
                Orders -> "APP.GENERAL.ORDERS"
                Trades -> "APP.GENERAL.TRADES"
                Fees -> "APP.GENERAL.FEES"
                Transfers -> "APP.GENERAL.TRANSFERS"
                Funding -> "APP.TRADE.FUNDING_PAYMENTS_SHORT"
            }

        val subTextStringKey: String
            get() = when (this) {
                Overview -> "APP.PORTFOLIO.OVERVIEW_DESCRIPTION"
                Positions -> "APP.PORTFOLIO.POSITIONS_DESCRIPTION"
                Orders -> "APP.PORTFOLIO.ORDERS_DESCRIPTION"
                Trades -> "APP.PORTFOLIO.TRADES_DESCRIPTION"
                Fees -> "APP.PORTFOLIO.FEE_STRUCTURE"
                Transfers -> "APP.PORTFOLIO.TRANSFERS_DESCRIPTION"
                Funding -> "APP.TRADE.FUNDING_PAYMENTS_DESCRIPTION"
            }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val displayContent: DisplayContent = DisplayContent.Overview,
        val tabSelection: DydxPortfolioSectionsView.Selection = DydxPortfolioSectionsView.Selection.Positions,
        val vaultEnabled: Boolean = false,
        val appRatingDialog: AppRatingDialog,
        val shouldLaunchAppRating: Boolean = false
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                appRatingDialog = AppRatingDialog(
                    localizer = MockLocalizer(),
                    showing = MutableStateFlow(true),
                ),
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

        if (state == null) {
            return
        }
        AppRatingDialogScaffold(dialog = state.appRatingDialog)

        val context = LocalContext.current
        LaunchedEffect(key1 = state.shouldLaunchAppRating) {
            if (state.shouldLaunchAppRating) {
                viewModel.launchAppRating(context)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val listState = PlatformRememberLazyListState(key = "DydxPortfolioView")

        val positionsViewModel: DydxPortfolioPositionsViewModel = hiltViewModel()
        val positionsViewState =
            positionsViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        val pendingPositionsViewModel: DydxPortfolioPendingPositionsViewModel = hiltViewModel()
        val pendingPositionsViewState =
            pendingPositionsViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        val ordersViewModel: DydxPortfolioOrdersViewModel = hiltViewModel()
        val ordersViewState =
            ordersViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        val fillsViewModel: DydxPortfolioFillsViewModel = hiltViewModel()
        val fillsViewState =
            fillsViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        val fundingsViewModel: DydxPortfolioFundingsViewModel = hiltViewModel()
        val fundingsViewState =
            fundingsViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

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
                        positionsListContent(positionsViewState)
                        pendingPositionsListContent(pendingPositionsViewState)
                    }

                    DydxPortfolioSectionsView.Selection.Orders -> {
                        ordersListContent(ordersViewState)
                    }

                    DydxPortfolioSectionsView.Selection.Trades -> {
                        fillsListContent(fillsViewState)
                    }

                    DydxPortfolioSectionsView.Selection.Funding -> {
                        fundingListContent(fundingsViewState)
                    }
                }

                if (state.vaultEnabled) {
                    item(key = "vault") {
                        DydxPortfolioVaultView.Content(Modifier)
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
            DydxPortfolioSelectorView.Content(Modifier)

            Spacer(modifier = Modifier.weight(1f))

            if (state.displayContent == DisplayContent.Overview || state.displayContent == DisplayContent.Transfers) {
                DydxPortfolioHeaderView.Content(modifier)
            }
        }
    }
}
