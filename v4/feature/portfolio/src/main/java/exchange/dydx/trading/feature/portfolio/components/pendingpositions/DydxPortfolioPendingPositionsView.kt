package exchange.dydx.trading.feature.portfolio.components.pendingpositions

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketPositionViewState

@Preview
@Composable
fun Preview_DydxPortfolioPendingPositionsView() {
    DydxThemedPreviewSurface {
        DydxPortfolioPendingPositionsView.Content(
            Modifier,
            DydxPortfolioPendingPositionsView.ViewState.preview
        )
    }
}

object DydxPortfolioPendingPositionsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val positions: List<DydxPortfolioPendingPositionView.ViewState> = listOf(),

        ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                positions = listOf(
                    DydxPortfolioPendingPositionView.ViewState.preview,
                    DydxPortfolioPendingPositionView.ViewState.preview,
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
        if (state == null) {
            return
        }
    }
}

