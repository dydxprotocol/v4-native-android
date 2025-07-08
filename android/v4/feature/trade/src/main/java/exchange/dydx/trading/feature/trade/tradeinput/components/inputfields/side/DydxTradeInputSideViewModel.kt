package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.side

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTradeInputSideViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeInputSideView.ViewState?> =
        abacusStateManager.state.tradeInput
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(tradeInput: TradeInput?): DydxTradeInputSideView.ViewState {
        return DydxTradeInputSideView.ViewState(
            localizer = localizer,
            sides = tradeInput?.options?.sideOptions?.toList()?.mapNotNull {
                it.string ?: localizer.localize(it.stringKey ?: return@mapNotNull null)
            } ?: listOf(),
            selectedIndex = tradeInput?.options?.sideOptions?.indexOfFirst {
                it.type == tradeInput.side?.rawValue
            } ?: 0,
            onSelectionChanged = { index ->
                val side = tradeInput?.options?.sideOptions?.getOrNull(index)?.type
                if (side != null) {
                    abacusStateManager.trade(side, TradeInputField.side)
                }
            },
            color = when (tradeInput?.side) {
                OrderSide.Buy -> ThemeColor.SemanticColor.positiveColor
                OrderSide.Sell -> ThemeColor.SemanticColor.negativeColor
                else -> ThemeColor.SemanticColor.text_primary
            },
        )
    }
}
