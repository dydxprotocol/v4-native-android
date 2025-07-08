package exchange.dydx.trading.feature.trade.orderbook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.trade.orderbook.components.DydxOrderbookSideView
import exchange.dydx.trading.feature.trade.orderbook.components.DydxOrderbookSpreadView

@Preview
@Composable
fun Preview_DydxOrderbookView() {
    DydxThemedPreviewSurface {
        DydxOrderbookView.Content(Modifier, DydxOrderbookView.ViewState.preview)
    }
}

object DydxOrderbookView : DydxComponent {

    data class ViewState(
        val localizer: LocalizerProtocol,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxOrderbookViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DydxOrderbookSideView.AsksContent(
                modifier = Modifier.weight(1f),
            )

            Row() {
                DydxOrderbookSpreadView.Content(Modifier)
            }

            DydxOrderbookSideView.BidsContent(
                modifier = Modifier.weight(1f),
            )
        }
    }
}
