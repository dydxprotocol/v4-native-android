package exchange.dydx.trading.feature.market.marketinfo.components.orderbook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.trade.orderbook.components.DydxOrderbookGroupView
import exchange.dydx.trading.feature.trade.orderbook.components.DydxOrderbookSideView
import exchange.dydx.trading.feature.trade.orderbook.components.DydxOrderbookSpreadView

@Preview
@Composable
fun Preview_DydxMarketOrderbookView() {
    DydxThemedPreviewSurface {
        DydxMarketOrderbookView.Content(Modifier, DydxMarketOrderbookView.ViewState.preview)
    }
}

object DydxMarketOrderbookView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val text: String?,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                text = "1.0M",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketOrderbookViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Column(
            modifier = modifier,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DydxOrderbookSpreadView.Content(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.weight(1f))
                DydxOrderbookGroupView.Content(modifier = Modifier.weight(1f))
            }

            PlatformDivider()

            Row(
                modifier = Modifier.padding(vertical = ThemeShapes.VerticalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DydxOrderbookSideView.AsksContent(
                    modifier = Modifier.weight(1f),
                    displayStyle = DydxOrderbookSideView.DisplayStyle.SideBySide,
                )
                DydxOrderbookSideView.BidsContent(
                    modifier = Modifier.weight(1f),
                    displayStyle = DydxOrderbookSideView.DisplayStyle.SideBySide,
                )
            }
        }
    }
}
