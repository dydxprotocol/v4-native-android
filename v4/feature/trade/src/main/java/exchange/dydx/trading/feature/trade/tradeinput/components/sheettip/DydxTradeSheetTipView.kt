package exchange.dydx.trading.feature.trade.tradeinput.components.sheettip

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxTradeSheetTipView() {
    DydxThemedPreviewSurface {
        DydxTradeSheetTipView.Content(Modifier, DydxTradeSheetTipView.ViewState.preview)
    }
}

object DydxTradeSheetTipView : DydxComponent {

    enum class TipState {
        BuySell, Draft
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val tipState: TipState,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                tipState = TipState.BuySell,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeSheetTipViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        when (state.tipState) {
            TipState.BuySell -> {
                DydxTradeSheetTipBuySellView.Content(modifier)
            }
            TipState.Draft -> {
                DydxTradeSheetTipDraftView.Content(modifier)
            }
        }
    }
}
