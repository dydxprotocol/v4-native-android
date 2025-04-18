package exchange.dydx.trading.feature.trade.trigger

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.output.account.SubaccountPosition
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.TriggerOrdersInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.stopLossOrders
import exchange.dydx.dydxstatemanager.takeProfitOrders
import exchange.dydx.trading.common.BuildConfig
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.PortfolioRoutes
import exchange.dydx.trading.feature.trade.streams.MutableTriggerOrderStreaming
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
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

    private val marketIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    private val marketId: String? = savedStateHandle["marketId"]

    private val includeLimitOrders = abacusStateManager.environment?.featureFlags?.isSlTpLimitOrdersEnabled == true || BuildConfig.DEBUG

    val state: Flow<DydxTriggerOrderInputView.ViewState?> =
        combine(
            abacusStateManager.state.validationErrors,
            marketIdFlow.filterNotNull().flatMapLatest { abacusStateManager.state.takeProfitOrders(it, includeLimitOrders) },
            marketIdFlow.filterNotNull().flatMapLatest { abacusStateManager.state.stopLossOrders(it, includeLimitOrders) },
        ) { validationErrors, takeProfitOrders, stopLossOrders ->
            createViewState(validationErrors, takeProfitOrders, stopLossOrders)
        }
            .distinctUntilChanged()

    init {
        if (marketId == null) {
            router.navigateBack()
        } else {
            marketIdFlow.value = marketId
            abacusStateManager.setMarket(marketId = marketId)
            abacusStateManager.resetTriggerOrders()
            abacusStateManager.triggerOrders(
                input = marketId,
                type = TriggerOrdersInputField.marketId,
            )

            combine(
                abacusStateManager.state.selectedSubaccountPositionOfMarket(marketId),
                abacusStateManager.state.takeProfitOrders(marketId, includeLimitOrders),
                abacusStateManager.state.stopLossOrders(marketId, includeLimitOrders),
                abacusStateManager.state.triggerOrdersInput
                    .filter { it?.marketId == marketId }
                    .distinctUntilChanged(),
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
        errors: List<ValidationError>?,
        takeProfitOrders: List<SubaccountOrder>?,
        stopLossOrders: List<SubaccountOrder>?,
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
                        DydxTriggerOrderInputView.ValidationErrorSection.Last
                }
            } ?: DydxTriggerOrderInputView.ValidationErrorSection.Last,
            hasMultipleTP = (takeProfitOrders?.size ?: 0) > 1,
            hasMultipleSL = (stopLossOrders?.size ?: 0) > 1,
            showOrderListAction = {
                router.navigateTo(
                    route = PortfolioRoutes.orders,
                    presentation = DydxRouter.Presentation.Push,
                )
            },
            showLimitPrice = includeLimitOrders,
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
                    if (triggerOrdersInput?.takeProfitOrder?.type == OrderType.TakeProfitLimit) {
                        abacusStateManager.triggerOrders(
                            formatter.decimalLocaleAgnostic(order.price),
                            TriggerOrdersInputField.takeProfitLimitPrice,
                        )
                    }
                }
            }
        } else {
            if (triggerOrdersInput?.takeProfitOrder?.type == null) {
                abacusStateManager.triggerOrders(
                    OrderType.TakeProfitMarket.rawValue,
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
                    if (triggerOrdersInput?.stopLossOrder?.type == OrderType.StopLimit) {
                        abacusStateManager.triggerOrders(
                            formatter.decimalLocaleAgnostic(order.price),
                            TriggerOrdersInputField.stopLossLimitPrice,
                        )
                    }
                }
            }
        } else {
            if (triggerOrdersInput?.stopLossOrder?.type == null) {
                abacusStateManager.triggerOrders(
                    OrderType.StopMarket.rawValue,
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
