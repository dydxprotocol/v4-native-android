package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.goodtil

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LabeledSelectionInput
import exchange.dydx.trading.feature.shared.views.LabeledTextInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTradeInputGoodTilViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeInputGoodTilView.ViewState?> =
        abacusStateManager.state.tradeInput
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(tradeInput: TradeInput?): DydxTradeInputGoodTilView.ViewState {
        return DydxTradeInputGoodTilView.ViewState(
            localizer = localizer,
            labeledTextInput = LabeledTextInput.ViewState(
                localizer = localizer,
                label = localizer.localize("APP.TRADE.GOOD_TIL"),
                value = tradeInput?.goodTil?.duration?.let {
                    formatter.raw(it, 0)
                },
                onValueChanged = { value ->
                    abacusStateManager.trade(value, TradeInputField.goodTilDuration)
                },
            ),
            labeledSelectionInput = LabeledSelectionInput.ViewState(
                localizer = localizer,
                options = tradeInput?.options?.goodTilUnitOptions?.toList()?.mapNotNull {
                    it.string ?: localizer.localize(it.stringKey ?: return@mapNotNull null)
                } ?: listOf(),
                selectedIndex = tradeInput?.options?.goodTilUnitOptions?.indexOfFirst {
                    it.type == tradeInput.goodTil?.unit
                } ?: 0,
                onSelectionChanged = { index ->
                    val type = tradeInput?.options?.goodTilUnitOptions?.getOrNull(index)?.type
                    if (type != null) {
                        abacusStateManager.trade(type, TradeInputField.goodTilUnit)
                    }
                },
            ),
        )
    }
}
