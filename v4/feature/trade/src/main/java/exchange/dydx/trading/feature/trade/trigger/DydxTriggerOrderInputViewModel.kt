package exchange.dydx.trading.feature.trade.trigger

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.SubaccountPosition
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.stopLossOrders
import exchange.dydx.dydxstatemanager.takeProfitOrders
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.trade.streams.MutableTriggerOrderStreaming
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTriggerOrderInputViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val formatter: DydxFormatter,
    savedStateHandle: SavedStateHandle,
    private val triggerOrderStream: MutableTriggerOrderStreaming,
    @CoroutineScopes.ViewModel private val viewModelScope: CoroutineScope,
) : ViewModel(), DydxViewModel {

    private val marketId: String?

    val state: Flow<DydxTriggerOrderInputView.ViewState?> =
        abacusStateManager.state.validationErrors
            .map { validationErrors ->
                createViewState(validationErrors)
            }
            .distinctUntilChanged()

    init {
        marketId = savedStateHandle["marketId"]

        if (marketId == null) {
            router.navigateBack()
        } else {
            abacusStateManager.setMarket(marketId = marketId)
            abacusStateManager.triggerOrders(
                input = marketId,
                type = TriggerOrdersInputField.marketId,
            )

            combine(
                abacusStateManager.state.selectedSubaccountPositionOfMarket(marketId),
                abacusStateManager.state.takeProfitOrders(marketId),
                abacusStateManager.state.stopLossOrders(marketId),
                abacusStateManager.state.triggerOrdersInput,
            ) { position, takeProfitOrders, stopLossOrders, triggerOrdersInput ->
                updateAbacusTriggerOrder(
                    position,
                    takeProfitOrders,
                    stopLossOrders,
                    triggerOrdersInput,
                )
            }
                .launchIn(viewModelScope)
        }
    }

    private fun createViewState(
        errors: List<ValidationError>?
    ): DydxTriggerOrderInputView.ViewState {
        val firstError = errors?.firstOrNull { it.type == ErrorType.error }
        val firstWarning = errors?.firstOrNull { it.type == ErrorType.warning }
        val fieldString = firstError?.fields?.firstOrNull() ?: firstWarning?.fields?.firstOrNull()
        val field: TriggerOrdersInputField? = fieldString?.let {
            TriggerOrdersInputField.invoke(it)
        }
        return DydxTriggerOrderInputView.ViewState(
            localizer = localizer,
            closeAction = {
                router.navigateBack()
            },
            backHandler = {
                abacusStateManager.resetTriggerOrders()
                triggerOrderStream.clearSubmissionStatus()
            },
            validationErrorSection = field?.let {
                when (it) {
                    TriggerOrdersInputField.size ->
                        DydxTriggerOrderInputView.ValidationErrorSection.Size

                    TriggerOrdersInputField.takeProfitPrice,
                    TriggerOrdersInputField.takeProfitUsdcDiff,
                    TriggerOrdersInputField.takeProfitPercentDiff ->
                        DydxTriggerOrderInputView.ValidationErrorSection.TakeProfit

                    TriggerOrdersInputField.stopLossPrice,
                    TriggerOrdersInputField.stopLossUsdcDiff,
                    TriggerOrdersInputField.stopLossPercentDiff ->
                        DydxTriggerOrderInputView.ValidationErrorSection.StopLoss

                    TriggerOrdersInputField.takeProfitLimitPrice,
                    TriggerOrdersInputField.stopLossLimitPrice ->
                        DydxTriggerOrderInputView.ValidationErrorSection.LimitPrice

                    else ->
                        DydxTriggerOrderInputView.ValidationErrorSection.None
                }
            } ?: DydxTriggerOrderInputView.ValidationErrorSection.None,
        )
    }

    private fun updateAbacusTriggerOrder(
        position: SubaccountPosition?,
        takeProfitOrders: List<SubaccountOrder>?,
        stopLossOrders: List<SubaccountOrder>?,
        triggerOrdersInput: TriggerOrdersInput?,
    ) {
        var takeProfitOrderSize = 0.0
        if (takeProfitOrders?.size == 1) {
            takeProfitOrders.first()?.let { order ->
                takeProfitOrderSize = order.size
                if (triggerOrdersInput?.takeProfitOrder?.orderId == null) {
                    abacusStateManager.triggerOrders(
                        order.id,
                        TriggerOrdersInputField.takeProfitOrderId,
                    )
                    abacusStateManager.triggerOrders(
                        formatter.decimalLocaleAgnostic(order.size),
                        TriggerOrdersInputField.takeProfitOrderSize,
                    )
                    abacusStateManager.triggerOrders(
                        order.type.rawValue,
                        TriggerOrdersInputField.takeProfitOrderType,
                    )
                    abacusStateManager.triggerOrders(
                        formatter.decimalLocaleAgnostic(order.triggerPrice),
                        TriggerOrdersInputField.takeProfitPrice,
                    )
                    abacusStateManager.triggerOrders(
                        formatter.decimalLocaleAgnostic(order.price),
                        TriggerOrdersInputField.takeProfitLimitPrice,
                    )
                }
            }
        } else {
            if (triggerOrdersInput?.takeProfitOrder?.type == null) {
                abacusStateManager.triggerOrders(
                    OrderType.takeProfitMarket.rawValue,
                    TriggerOrdersInputField.takeProfitOrderType,
                )
            }
        }

        var stopLossOrderSize = 0.0
        if (stopLossOrders?.size == 1) {
            stopLossOrders.first()?.let { order ->
                stopLossOrderSize = order.size
                if (triggerOrdersInput?.stopLossOrder?.orderId == null) {
                    abacusStateManager.triggerOrders(
                        order.id,
                        TriggerOrdersInputField.stopLossOrderId,
                    )
                    abacusStateManager.triggerOrders(
                        formatter.decimalLocaleAgnostic(order.size),
                        TriggerOrdersInputField.stopLossOrderSize,
                    )
                    abacusStateManager.triggerOrders(
                        order.type.rawValue,
                        TriggerOrdersInputField.stopLossOrderType,
                    )
                    abacusStateManager.triggerOrders(
                        formatter.decimalLocaleAgnostic(order.triggerPrice),
                        TriggerOrdersInputField.stopLossPrice,
                    )
                    abacusStateManager.triggerOrders(
                        formatter.decimalLocaleAgnostic(order.price),
                        TriggerOrdersInputField.stopLossLimitPrice,
                    )
                }
            }
        } else {
            if (triggerOrdersInput?.stopLossOrder?.type == null) {
                abacusStateManager.triggerOrders(
                    OrderType.stopMarket.rawValue,
                    TriggerOrdersInputField.stopLossOrderType,
                )
            }
        }

        if (triggerOrdersInput?.size == null) {
            if (takeProfitOrderSize == 0.0 && stopLossOrderSize == 0.0) {
                // defaulting to position size
                abacusStateManager.triggerOrders(
                    formatter.decimalLocaleAgnostic(position?.size?.current),
                    TriggerOrdersInputField.size,
                )
            } else if (takeProfitOrderSize > 0.0 && stopLossOrderSize > 0.0 && takeProfitOrderSize != stopLossOrderSize) {
                // different order size
                abacusStateManager.triggerOrders(
                    null,
                    TriggerOrdersInputField.size,
                )
            } else if (takeProfitOrderSize > 0.0) {
                abacusStateManager.triggerOrders(
                    formatter.decimalLocaleAgnostic(takeProfitOrderSize),
                    TriggerOrdersInputField.size,
                )
            } else if (stopLossOrderSize > 0.0) {
                abacusStateManager.triggerOrders(
                    formatter.decimalLocaleAgnostic(stopLossOrderSize),
                    TriggerOrdersInputField.size,
                )
            }
        }
    }
}
