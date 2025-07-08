package exchange.dydx.trading.feature.portfolio.components.overview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.tabgroups.PlatformTextTabGroup
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxPortfolioSectionsView() {
    DydxThemedPreviewSurface {
        DydxPortfolioSectionsView.Content(Modifier, DydxPortfolioSectionsView.ViewState.preview)
    }
}

object DydxPortfolioSectionsView : DydxComponent {

    enum class Selection {
        Positions, Orders, Trades, Funding;

        val stringKey: String
            get() = when (this) {
                Positions -> "APP.TRADE.POSITIONS"
                Orders -> "APP.GENERAL.ORDERS"
                Trades -> "APP.GENERAL.TRADES"
                Funding -> "APP.TRADE.FUNDING_PAYMENTS_SHORT"
            }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val selections: List<Selection> = listOf(
            Selection.Positions,
            Selection.Orders,
            Selection.Trades,
            Selection.Funding,
        ),
        val currentSelection: Selection = Selection.Positions,
        val onSelectionChanged: (Selection) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxPortfolioSectionsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        val items = state.selections.map { selection ->
            state.localizer.localize(selection.stringKey)
        }

        PlatformTextTabGroup(
            modifier = modifier,
            items = items,
            selectedItems = items,
            currentSelection = state.selections.indexOf(state.currentSelection),
            onSelectionChanged = { index ->
                state.onSelectionChanged(state.selections[index])
            },
        )
    }
}
