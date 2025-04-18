package exchange.dydx.trading.feature.trade.trigger.components.inputfields.price

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.TriggerOrdersInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.platformui.components.inputs.PlatformInputAlertState
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LabeledTextInput
import exchange.dydx.trading.feature.trade.alertState
import exchange.dydx.trading.feature.trade.trigger.components.inputfields.DydxTriggerOrderPriceInputType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTriggerOrderTakeProfitPriceViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : DydxTriggerOrderPriceViewModel(
    localizer,
    abacusStateManager,
    formatter,
    DydxTriggerOrderPriceInputType.TakeProfit,
)

@HiltViewModel
class DydxTriggerOrderStopLossPriceViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : DydxTriggerOrderPriceViewModel(
    localizer,
    abacusStateManager,
    formatter,
    DydxTriggerOrderPriceInputType.StopLoss,
)

@HiltViewModel
class DydxTriggerOrderTakeProfitLimitPriceViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : DydxTriggerOrderPriceViewModel(
    localizer,
    abacusStateManager,
    formatter,
    DydxTriggerOrderPriceInputType.TakeProfitLimit,
)

@HiltViewModel
class DydxTriggerOrderStopLossLimitPriceViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : DydxTriggerOrderPriceViewModel(
    localizer,
    abacusStateManager,
    formatter,
    DydxTriggerOrderPriceInputType.StopLossLimit,
)

open class DydxTriggerOrderPriceViewModel(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    val inputType: DydxTriggerOrderPriceInputType,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTriggerOrderPriceView.ViewState?> =
        combine(
            abacusStateManager.state.triggerOrdersInput,
            abacusStateManager.state.configsAndAssetMap,
            abacusStateManager.state.validationErrors,
        ) { triggerOrdersInput, configsAndAssetMap, validationErrors ->
            val marketId = triggerOrdersInput?.marketId ?: return@combine null
            createViewState(triggerOrdersInput, configsAndAssetMap?.get(marketId), validationErrors)
        }
            .distinctUntilChanged()

    private fun createViewState(
        triggerOrdersInput: TriggerOrdersInput?,
        configsAndAsset: MarketConfigsAndAsset?,
        validationErrors: List<ValidationError>?,
    ): DydxTriggerOrderPriceView.ViewState {
        val marketConfigs = configsAndAsset?.configs
        val tickSize = marketConfigs?.displayTickSizeDecimals ?: 0
        val firstErrorOrWarning = validationErrors?.firstOrNull { it.type == ErrorType.error }
            ?: validationErrors?.firstOrNull { it.type == ErrorType.warning }

        val state = DydxTriggerOrderPriceView.ViewState(
            localizer = localizer,
            labeledTextInput = LabeledTextInput.ViewState(
                localizer = localizer,
                label = when (inputType) {
                    DydxTriggerOrderPriceInputType.TakeProfit -> localizer.localize("APP.TRIGGERS_MODAL.TP_PRICE")
                    DydxTriggerOrderPriceInputType.StopLoss -> localizer.localize("APP.TRIGGERS_MODAL.SL_PRICE")
                    DydxTriggerOrderPriceInputType.TakeProfitLimit -> localizer.localize("APP.TRIGGERS_MODAL.TP_LIMIT")
                    DydxTriggerOrderPriceInputType.StopLossLimit -> localizer.localize("APP.TRIGGERS_MODAL.SL_LIMIT")
                },
                token = "USD",
                value = when (inputType) {
                    DydxTriggerOrderPriceInputType.TakeProfit -> formatter.raw(
                        triggerOrdersInput?.takeProfitOrder?.price?.triggerPrice,
                        tickSize,
                    )

                    DydxTriggerOrderPriceInputType.StopLoss -> formatter.raw(
                        triggerOrdersInput?.stopLossOrder?.price?.triggerPrice,
                        tickSize,
                    )

                    DydxTriggerOrderPriceInputType.TakeProfitLimit -> formatter.raw(
                        triggerOrdersInput?.takeProfitOrder?.price?.limitPrice,
                        tickSize,
                    )

                    DydxTriggerOrderPriceInputType.StopLossLimit -> formatter.raw(
                        triggerOrdersInput?.stopLossOrder?.price?.limitPrice,
                        tickSize,
                    )
                },
                alertState = if (firstErrorOrWarning?.fields?.contains(inputType.abacusInputField.rawValue) == true) {
                    firstErrorOrWarning.alertState
                } else {
                    PlatformInputAlertState.None
                },
                placeholder = formatter.raw(0.0, tickSize),
                onValueChanged = { value ->
                    when (inputType) {
                        DydxTriggerOrderPriceInputType.TakeProfit -> abacusStateManager.triggerOrders(
                            value,
                            TriggerOrdersInputField.takeProfitPrice,
                        )

                        DydxTriggerOrderPriceInputType.StopLoss -> abacusStateManager.triggerOrders(
                            value,
                            TriggerOrdersInputField.stopLossPrice,
                        )

                        DydxTriggerOrderPriceInputType.TakeProfitLimit -> abacusStateManager.triggerOrders(
                            value,
                            TriggerOrdersInputField.takeProfitLimitPrice,
                        )

                        DydxTriggerOrderPriceInputType.StopLossLimit -> abacusStateManager.triggerOrders(
                            value,
                            TriggerOrdersInputField.stopLossLimitPrice,
                        )
                    }
                },
            ),
        )

        return state
    }
}

val DydxTriggerOrderPriceInputType.abacusInputField: TriggerOrdersInputField
    get() = when (this) {
        DydxTriggerOrderPriceInputType.TakeProfit -> TriggerOrdersInputField.takeProfitPrice
        DydxTriggerOrderPriceInputType.StopLoss -> TriggerOrdersInputField.stopLossPrice
        DydxTriggerOrderPriceInputType.TakeProfitLimit -> TriggerOrdersInputField.takeProfitLimitPrice
        DydxTriggerOrderPriceInputType.StopLossLimit -> TriggerOrdersInputField.stopLossLimitPrice
    }
