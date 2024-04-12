package exchange.dydx.trading.feature.trade.trigger.components.inputfields.gainloss

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.output.input.TriggerPrice
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LabeledSelectionInput
import exchange.dydx.trading.feature.shared.views.LabeledTextInput
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
    val inputType: DydxTriggerOrderInputType,
    private val triggerOrderStream: MutableTriggerOrderStreaming,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTriggerOrderGainLossView.ViewState?> =
        combine(
            when (inputType) {
                DydxTriggerOrderInputType.TakeProfit -> triggerOrderStream.takeProfitGainLossDisplayType
                DydxTriggerOrderInputType.StopLoss -> triggerOrderStream.stopLossGainLossDisplayType
            },
            abacusStateManager.state.triggerOrdersInput,
            abacusStateManager.state.configsAndAssetMap,
        ) { displayType, triggerOrdersInput, configsAndAssetMap ->
            val marketId = triggerOrdersInput?.marketId ?: return@combine null
            createViewState(displayType, triggerOrdersInput, configsAndAssetMap?.get(marketId))
        }
            .distinctUntilChanged()

    private fun createViewState(
        displayType: GainLossDisplayType,
        triggerOrdersInput: TriggerOrdersInput?,
        configsAndAsset: MarketConfigsAndAsset?,
    ): DydxTriggerOrderGainLossView.ViewState {
        val marketConfigs = configsAndAsset?.configs
        val tickSize = marketConfigs?.displayTickSizeDecimals ?: 0

        fun formatOrder(orderPrice: TriggerPrice) =
            when (displayType) {
                GainLossDisplayType.Amount -> formatter.raw(orderPrice.usdcDiff, tickSize)
                GainLossDisplayType.Percent -> orderPrice.percentDiff?.let { formatter.percent(it / 100.0, 2) }
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
                placeholder = formatter.raw(0.0, tickSize),
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
                        DydxTriggerOrderInputType.TakeProfit -> triggerOrderStream.setTakeProfitGainLossDisplayType(GainLossDisplayType.list[index])
                        DydxTriggerOrderInputType.StopLoss -> triggerOrderStream.setStopLossGainLossDisplayType(GainLossDisplayType.list[index])
                    }
                },
            ),
        )
    }
}
