package exchange.dydx.trading.feature.trade.tradeinput

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.receipt.ReceiptType
import exchange.dydx.trading.feature.receipt.TradeReceiptType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTradeInputViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    val receiptTypeFlow: MutableStateFlow<@JvmSuppressWildcards ReceiptType?>,
    val orderbookToggleStateFlow: Flow<@JvmSuppressWildcards DydxTradeInputView.OrderbookToggleState>,
    val buttomSheetStateFlow: MutableStateFlow<@JvmSuppressWildcards DydxTradeInputView.BottomSheetState?>,
) : ViewModel(), DydxViewModel {

    init {
        receiptTypeFlow.value = ReceiptType.Trade(TradeReceiptType.Open)
    }

    val state: Flow<DydxTradeInputView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput,
            orderbookToggleStateFlow,
            buttomSheetStateFlow,
        ) { tradeInput, orderbookToggleState, buttomSheetState ->
            createViewState(tradeInput, orderbookToggleState, buttomSheetState)
        }
            .distinctUntilChanged()

    private fun createViewState(
        tradeInput: TradeInput?,
        orderbookToggleState: DydxTradeInputView.OrderbookToggleState,
        buttomSheetState: DydxTradeInputView.BottomSheetState?,
    ): DydxTradeInputView.ViewState {
        return DydxTradeInputView.ViewState(
            localizer = localizer,
            inputFields = listOfNotNull(
                if (tradeInput?.options?.needsSize == true) DydxTradeInputView.InputField.Size else null,
                if (tradeInput?.options?.needsSize == true && tradeInput.options?.needsLeverage == true && tradeInput.marginMode == MarginMode.Cross) {
                    DydxTradeInputView.InputField.Leverage
                } else {
                    null
                },
                if (tradeInput?.options?.needsLimitPrice == true) DydxTradeInputView.InputField.LimitPrice else null,
                if (tradeInput?.options?.needsTriggerPrice == true) DydxTradeInputView.InputField.TriggerPrice else null,
                if (tradeInput?.options?.needsTrailingPercent == true) DydxTradeInputView.InputField.TrailingPercent else null,
                if (tradeInput?.options?.timeInForceOptions?.isNotEmpty() == true) DydxTradeInputView.InputField.TimeInForce else null,
                if (tradeInput?.options?.needsGoodUntil == true) DydxTradeInputView.InputField.GoodTil else null,
                if (tradeInput?.options?.executionOptions?.isNotEmpty() == true) DydxTradeInputView.InputField.Execution else null,
                if (tradeInput?.options?.needsPostOnly == true) DydxTradeInputView.InputField.PostOnly else null,
                if (tradeInput?.options?.needsReduceOnly == true) DydxTradeInputView.InputField.ReduceOnly else null,
            ),
            orderbookToggleState = orderbookToggleState,
            requestedBottomSheetState = buttomSheetState,
            onRequestedBottomSheetStateCompleted = {
                buttomSheetStateFlow.value = null
            },
        )
    }
}
