package exchange.dydx.trading.feature.profile.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.portfolio.components.fills.DydxPortfolioFillsView.fillsListContent
import exchange.dydx.trading.feature.portfolio.components.fills.DydxPortfolioFillsViewModel
import exchange.dydx.trading.feature.portfolio.components.fundings.DydxPortfolioFundingsView.fundingListContent
import exchange.dydx.trading.feature.portfolio.components.fundings.DydxPortfolioFundingsViewModel
import exchange.dydx.trading.feature.portfolio.components.transfers.DydxPortfolioTransfersView.transferListContent
import exchange.dydx.trading.feature.portfolio.components.transfers.DydxPortfolioTransfersViewModel
import exchange.dydx.trading.feature.shared.views.HeaderView
import exchange.dydx.trading.feature.shared.views.SelectionBar

@Preview
@Composable
fun Preview_DydxHistoryView() {
    DydxThemedPreviewSurface {
        DydxHistoryView.Content(Modifier, DydxHistoryView.ViewState.preview)
    }
}

object DydxHistoryView : DydxComponent {

    data class ViewState(
        val localizer: LocalizerProtocol,
        val selectionBarViewState: SelectionBar.ViewState?,
        val backButtionAction: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                selectionBarViewState = SelectionBar.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxHistoryViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state?.selectionBarViewState == null) {
            return
        }

        val listState = rememberLazyListState()

        val fillsViewModel: DydxPortfolioFillsViewModel = hiltViewModel()
        val fillsViewState = fillsViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        val transfersViewModel: DydxPortfolioTransfersViewModel = hiltViewModel()
        val transfersViewState = transfersViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        val fundingsViewModel: DydxPortfolioFundingsViewModel = hiltViewModel()
        val fundingsViewState = fundingsViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        val currentSelection: MutableState<Int?> = remember {
            mutableStateOf(
                state.selectionBarViewState.currentSelection,
            )
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.GENERAL.HISTORY"),
                modifier = Modifier.fillMaxWidth(),
                backAction = state.backButtionAction,
            )

            SelectionBar.Content(
                modifier = Modifier.fillMaxWidth(),
                state = state.selectionBarViewState,
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
            ) {
                if (state.selectionBarViewState.currentSelection == 0) {
                    fillsListContent(fillsViewState)
                } else if (state.selectionBarViewState.currentSelection == 1) {
                    transferListContent(transfersViewState)
                } else if (state.selectionBarViewState.currentSelection == 2) {
                    fundingListContent(fundingsViewState)
                }
            }
        }

        if (currentSelection.value != state.selectionBarViewState.currentSelection) {
            currentSelection.value = state.selectionBarViewState.currentSelection
            LaunchedEffect(key1 = "scrollToTop") {
                listState.scrollToItem(0)
            }
        }
    }
}
