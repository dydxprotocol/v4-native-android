package exchange.dydx.trading.feature.receipt.components.isolatedmargin

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.TradeStatesWithDoubleValues
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LeverageView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxReceiptIsolatedPositionLeverageViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxReceiptIsolatedPositionLeverageView.ViewState?> =
        abacusStateManager.marketId
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        marketId: String?
    ): DydxReceiptIsolatedPositionLeverageView.ViewState {
        val position = if (marketId != null) abacusStateManager.state.selectedSubaccountPositionOfMarket(marketId).value else null
        val leverage: TradeStatesWithDoubleValues? = position?.leverage
        val margin: TradeStatesWithDoubleValues? = null // position?.margin
        /*
        TODO: After abacus exposes Leverage, changes to next line
        if (marketId != null) abacusStateManager.state.selectedSubaccountPositionOfMarket(marketId).value?.leverage else null
         */
        return DydxReceiptIsolatedPositionLeverageView.ViewState(
            localizer = localizer,
            formatter = formatter,
            before = if (leverage?.current != null) {
                LeverageView.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    leverage = leverage.current ?: 0.0,
                    margin = margin?.current,
                )
            } else {
                null
            },
            after = if (leverage?.postOrder != null) {
                LeverageView.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    leverage = leverage.postOrder ?: 0.0,
                    margin = margin?.postOrder,
                )
            } else {
                null
            },
        )
    }
}
