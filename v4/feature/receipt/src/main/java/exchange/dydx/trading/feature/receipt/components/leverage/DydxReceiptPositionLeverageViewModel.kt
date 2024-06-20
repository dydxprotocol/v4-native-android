package exchange.dydx.trading.feature.receipt.components.leverage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.SubaccountPosition
import exchange.dydx.abacus.output.TradeStatesWithDoubleValues
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LeverageView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxReceiptPositionLeverageViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxReceiptPositionLeverageView.ViewState?> =
        abacusStateManager.marketId
            .filterNotNull()
            .flatMapLatest { marketId ->
                abacusStateManager.state.selectedSubaccountPositionOfMarket(marketId)}
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        position: SubaccountPosition?
    ): DydxReceiptPositionLeverageView.ViewState {
        val leverage: TradeStatesWithDoubleValues? = position?.leverage
        val margin: TradeStatesWithDoubleValues? =  position?.marginUsage
        return DydxReceiptPositionLeverageView.ViewState(
            localizer = localizer,
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
