package exchange.dydx.trading.feature.trade.trigger.components.inputfields.gainloss

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.output.input.TriggerPrice
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.TriggerOrdersInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.inputs.PlatformInputAlertState
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LabeledSelectionInput
import exchange.dydx.trading.feature.shared.views.LabeledTextInput
import exchange.dydx.trading.feature.trade.alertState
import exchange.dydx.trading.feature.trade.streams.GainLossDisplayType
import exchange.dydx.trading.feature.trade.streams.MutableTriggerOrderStreaming
import exchange.dydx.trading.feature.trade.trigger.components.inputfields.DydxTriggerOrderInputType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTriggerOrderGainLossTakeProfitViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val triggerOrderStream: MutableTriggerOrderStreaming,
) : DydxTriggerOrderGainLossViewModel(
    localizer,
    abacusStateManager,
    formatter,
    DydxTriggerOrderInputType.TakeProfit,
    triggerOrderStream,
)

@HiltViewModel
class DydxTriggerOrderGainLossStopLossViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val triggerOrderStream: MutableTriggerOrderStreaming,
) : DydxTriggerOrderGainLossViewModel(
    localizer,
    abacusStateManager,
    formatter,
    DydxTriggerOrderInputType.StopLoss,
    triggerOrderStream,
)

open class DydxTriggerOrderGainLossViewModel(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val inputType: DydxTriggerOrderInputType,
    private val triggerOrderStream: MutableTriggerOrderStreaming,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTriggerOrderGainLossView.ViewState?> =
        combine(
            when (inputType) {
                DydxTriggerOrderInputType.TakeProfit -> triggerOrderStream.takeProfitGainLossDisplayType
                DydxTriggerOrderInputType.StopLoss -> triggerOrderStream.stopLossGainLossDisplayType
            },
            abacusStateManager.state.triggerOrdersInput,
            abacusStateManager.state.validationErrors,
        ) { displayType, triggerOrdersInput, validationErrors ->
            createViewState(displayType, triggerOrdersInput, validationErrors)
        }
            .distinctUntilChanged()

    private fun createViewState(
        displayType: GainLossDisplayType,
        triggerOrdersInput: TriggerOrdersInput?,
        validationErrors: List<ValidationError>?,
    ): DydxTriggerOrderGainLossView.ViewState {
        val firstErrorOrWarning = validationErrors?.firstOrNull { it.type == ErrorType.error }
            ?: validationErrors?.firstOrNull { it.type == ErrorType.warning }

        fun formatOrder(orderPrice: TriggerPrice) =
            when (displayType) {
                GainLossDisplayType.Amount -> formatter.raw(orderPrice.usdcDiff, 2)
                GainLossDisplayType.Percent -> orderPrice.percentDiff?.let {
                    formatter.raw(it, 2)
                }
            }

        val inputField: TriggerOrdersInputField = when (inputType) {
            DydxTriggerOrderInputType.TakeProfit ->
                when (displayType) {
                    GainLossDisplayType.Amount -> TriggerOrdersInputField.takeProfitUsdcDiff
                    GainLossDisplayType.Percent -> TriggerOrdersInputField.takeProfitPercentDiff
                }

            DydxTriggerOrderInputType.StopLoss ->
                when (displayType) {
                    GainLossDisplayType.Amount -> TriggerOrdersInputField.stopLossUsdcDiff
                    GainLossDisplayType.Percent -> TriggerOrdersInputField.stopLossPercentDiff
                }
        }

        return DydxTriggerOrderGainLossView.ViewState(
            localizer = localizer,
            labeledTextInput = LabeledTextInput.ViewState(
                localizer = localizer,
                label = when (inputType) {
                    DydxTriggerOrderInputType.TakeProfit -> localizer.localize("APP.GENERAL.GAIN")
                    DydxTriggerOrderInputType.StopLoss -> localizer.localize("APP.GENERAL.LOSS")
                },
                value = when (inputType) {
                    DydxTriggerOrderInputType.TakeProfit -> triggerOrdersInput?.takeProfitOrder?.price?.let {
                        formatOrder(it)
                    }

                    DydxTriggerOrderInputType.StopLoss -> triggerOrdersInput?.stopLossOrder?.price?.let {
                        formatOrder(it)
                    }
                },
                alertState = when (inputType) {
                    DydxTriggerOrderInputType.TakeProfit ->
                        if (firstErrorOrWarning?.fields?.contains(TriggerOrdersInputField.takeProfitPercentDiff.rawValue) == true ||
                            firstErrorOrWarning?.fields?.contains(TriggerOrdersInputField.takeProfitUsdcDiff.rawValue) == true
                        ) {
                            firstErrorOrWarning.alertState
                        } else {
                            PlatformInputAlertState.None
                        }

                    DydxTriggerOrderInputType.StopLoss ->
                        if (firstErrorOrWarning?.fields?.contains(TriggerOrdersInputField.stopLossPercentDiff.rawValue) == true ||
                            firstErrorOrWarning?.fields?.contains(TriggerOrdersInputField.stopLossUsdcDiff.rawValue) == true
                        ) {
                            firstErrorOrWarning.alertState
                        } else {
                            PlatformInputAlertState.None
                        }
                },
                placeholder = formatter.raw(0.0, 2),
                onValueChanged = { value ->
                    abacusStateManager.triggerOrders(value, inputField)
                },
            ),
            labeledSelectionInput = LabeledSelectionInput.ViewState(
                localizer = localizer,
                options = GainLossDisplayType.list.map { it.value },
                selectedIndex = GainLossDisplayType.list.indexOf(displayType),
                onSelectionChanged = { index ->
                    when (inputType) {
                        DydxTriggerOrderInputType.TakeProfit -> triggerOrderStream.setTakeProfitGainLossDisplayType(
                            GainLossDisplayType.list[index],
                        )

                        DydxTriggerOrderInputType.StopLoss -> triggerOrderStream.setStopLossGainLossDisplayType(
                            GainLossDisplayType.list[index],
                        )
                    }
                },
            ),
        )
    }
}
