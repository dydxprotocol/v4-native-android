package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.execution

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.shared.views.LabeledSelectionInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTradeInputExecutionViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeInputExecutionView.ViewState?> =
        abacusStateManager.state.tradeInput
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(tradeInput: TradeInput?): DydxTradeInputExecutionView.ViewState {
        return DydxTradeInputExecutionView.ViewState(
            localizer = localizer,
            labeledSelectionInput = LabeledSelectionInput.ViewState(
                localizer = localizer,
                label = localizer.localize("APP.TRADE.EXECUTION"),
                options = tradeInput?.options?.executionOptions?.toList()?.mapNotNull {
                    it.string ?: localizer.localize(it.stringKey ?: return@mapNotNull null)
                } ?: listOf(),
                selectedIndex = tradeInput?.options?.executionOptions?.indexOfFirst {
                    it.type == tradeInput.execution
                } ?: 0,
                onSelectionChanged = { index ->
                    val type = tradeInput?.options?.executionOptions?.getOrNull(index)?.type
                    if (type != null) {
                        abacusStateManager.trade(type, TradeInputField.execution)
                    }
                },
            ),
        )
    }
}
