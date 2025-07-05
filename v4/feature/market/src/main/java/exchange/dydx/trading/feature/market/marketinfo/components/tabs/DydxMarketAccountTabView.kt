package exchange.dydx.trading.feature.market.marketinfo.components.tabs

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
fun Preview_DydxMarketAccountTabView() {
    DydxThemedPreviewSurface {
        DydxMarketAccountTabView.Content(Modifier, DydxMarketAccountTabView.ViewState.preview)
    }
}

object DydxMarketAccountTabView : DydxComponent {

    enum class Selection {
        Position, Orders, Trades, Funding;

        val stringKey: String
            get() = when (this) {
                Position -> "APP.GENERAL.POSITION"
                Orders -> "APP.GENERAL.ORDERS"
                Trades -> "APP.GENERAL.TRADES"
                Funding -> "APP.TRADE.FUNDING_PAYMENTS_SHORT"
            }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val currentSelection: Selection = Selection.Position,
        val selections: List<Selection> = listOf(Selection.Position, Selection.Orders, Selection.Trades, Selection.Funding),
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
        val viewModel: DydxMarketAccountTabViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

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
