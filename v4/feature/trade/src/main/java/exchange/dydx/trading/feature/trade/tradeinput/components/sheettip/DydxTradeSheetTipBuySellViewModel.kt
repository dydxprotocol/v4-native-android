package exchange.dydx.trading.feature.trade.tradeinput.components.sheettip

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
import exchange.dydx.trading.feature.trade.tradeinput.DydxTradeInputView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTradeSheetTipBuySellViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val buttomSheetFlow: MutableStateFlow<@JvmSuppressWildcards DydxTradeInputView.BottomSheetState?>,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeSheetTipBuySellView.ViewState?> =
        abacusStateManager.state.tradeInput
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(tradeInput: TradeInput?): DydxTradeSheetTipBuySellView.ViewState {
        return DydxTradeSheetTipBuySellView.ViewState(
            localizer = localizer,
            sides = tradeInput?.options?.sideOptions?.toList()?.mapNotNull {
                val text = it.string ?: localizer.localize(it.stringKey ?: return@mapNotNull null)
                val color = when (it.type) {
                    OrderSide.Buy.rawValue -> ThemeColor.SemanticColor.positiveColor
                    OrderSide.Sell.rawValue -> ThemeColor.SemanticColor.negativeColor
                    else -> ThemeColor.SemanticColor.text_primary
                }
                text to color
            } ?: listOf(),
            onSelectionChanged = { index ->
                val side = tradeInput?.options?.sideOptions?.getOrNull(index)?.type
                if (side != null) {
                    abacusStateManager.trade(side, TradeInputField.side)
                }

                buttomSheetFlow.value = DydxTradeInputView.BottomSheetState.Expanded
            },
        )
    }
}
