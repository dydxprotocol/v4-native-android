package exchange.dydx.trading.feature.trade.trigger.components.inputfields.gainloss

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TriggerOrder
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LabeledSelectionInput
import exchange.dydx.trading.feature.shared.views.LabeledTextInput
import exchange.dydx.trading.feature.trade.trigger.components.inputfields.DydxTriggerOrderInputType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTriggerOrderGainLossTakeProfitViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : DydxTriggerOrderGainLossViewModel(
    localizer,
    abacusStateManager,
    formatter,
    DydxTriggerOrderInputType.TakeProfit,
)

@HiltViewModel
class DydxTriggerOrderGainLossStopLossViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : DydxTriggerOrderGainLossViewModel(
    localizer,
    abacusStateManager,
    formatter,
    DydxTriggerOrderInputType.StopLoss,
)

open class DydxTriggerOrderGainLossViewModel(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    val inputType: DydxTriggerOrderInputType,
) : ViewModel(), DydxViewModel {

    private enum class DisplayType {
        Amount,
        Percent;

        val value: String
            get() = when (this) {
                Amount -> "$"
                Percent -> "%"
            }

        companion object {
            val list = listOf(Amount, Percent)
        }
    }

    private val displayTypeFlow = MutableStateFlow(DisplayType.Amount)

    val state: Flow<DydxTriggerOrderGainLossView.ViewState?> =
        combine(
            displayTypeFlow,
            abacusStateManager.state.triggerOrdersInput,
            abacusStateManager.state.configsAndAssetMap,
        ) { displayType, triggerOrdersInput, configsAndAssetMap ->
            val marketId = triggerOrdersInput?.marketId ?: return@combine null
            createViewState(displayType, triggerOrdersInput, configsAndAssetMap?.get(marketId))
        }
            .distinctUntilChanged()

    private fun createViewState(
        displayType: DisplayType,
        triggerOrdersInput: TriggerOrdersInput?,
        configsAndAsset: MarketConfigsAndAsset?,
    ): DydxTriggerOrderGainLossView.ViewState {
        val marketConfigs = configsAndAsset?.configs
        val tickSize = marketConfigs?.displayTickSizeDecimals ?: 0

        fun formatOrder(order: TriggerOrder) =
            when (displayType) {
                DisplayType.Amount -> formatter.raw(order.price?.usdcDiff, tickSize)
                DisplayType.Percent -> formatter.raw(order.price?.percentDiff, 2)
            }

        val inputField: TriggerOrdersInputField = when (inputType) {
            DydxTriggerOrderInputType.TakeProfit ->
                when (displayType) {
                    DisplayType.Amount -> TriggerOrdersInputField.takeProfitUsdcDiff
                    DisplayType.Percent -> TriggerOrdersInputField.takeProfitPercentDiff
                }
            DydxTriggerOrderInputType.StopLoss ->
                when (displayType) {
                    DisplayType.Amount -> TriggerOrdersInputField.stopLossUsdcDiff
                    DisplayType.Percent -> TriggerOrdersInputField.stopLossPercentDiff
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
                    DydxTriggerOrderInputType.TakeProfit -> triggerOrdersInput?.takeProfitOrder ?.let {
                        formatOrder(it)
                    }
                    DydxTriggerOrderInputType.StopLoss -> triggerOrdersInput?.stopLossOrder?.let {
                        formatOrder(it)
                    }
                },
                placeholder = formatter.raw(0.0, tickSize),
                onValueChanged = { value ->
                    abacusStateManager.triggerOrders(value, inputField)
                },
            ),
            labeledSelectionInput = LabeledSelectionInput.ViewState(
                localizer = localizer,
                options = DisplayType.list.map { it.value },
                selectedIndex = DisplayType.list.indexOf(displayType),
                onSelectionChanged = { index ->
                    displayTypeFlow.value = DisplayType.list[index]
                },
            ),
        )
    }
}
