package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.reduceonly

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTradeInputReduceOnlyViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeInputReduceOnlyView.ViewState?> =
        abacusStateManager.state.tradeInput
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(tradeInput: TradeInput?): DydxTradeInputReduceOnlyView.ViewState {
        return DydxTradeInputReduceOnlyView.ViewState(
            localizer = localizer,
            value = tradeInput?.reduceOnly ?: false,
            onValueChanged = { value ->
                abacusStateManager.trade(if (value) "true" else "false", TradeInputField.reduceOnly)
            },
        )
    }
}
