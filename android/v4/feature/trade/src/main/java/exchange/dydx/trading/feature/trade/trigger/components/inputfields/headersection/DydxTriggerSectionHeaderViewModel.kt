package exchange.dydx.trading.feature.trade.trigger.components.inputfields.headersection

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.output.input.TriggerPrice
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.TriggerOrdersInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.trade.streams.GainLossDisplayType
import exchange.dydx.trading.feature.trade.streams.TriggerOrderStreaming
import exchange.dydx.trading.feature.trade.trigger.components.inputfields.DydxTriggerOrderInputType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.lang.Math.abs
import javax.inject.Inject

@HiltViewModel
class DydxTriggerSectionHeaderTakeProfitViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val triggerOrderStream: TriggerOrderStreaming,
) : DydxTriggerSectionHeaderViewModel(
    localizer,
    abacusStateManager,
    formatter,
    DydxTriggerOrderInputType.TakeProfit,
    triggerOrderStream,
)

@HiltViewModel
class DydxTriggerSectionHeaderStopLossViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val triggerOrderStream: TriggerOrderStreaming,
) : DydxTriggerSectionHeaderViewModel(
    localizer,
    abacusStateManager,
    formatter,
    DydxTriggerOrderInputType.StopLoss,
    triggerOrderStream,
)

open class DydxTriggerSectionHeaderViewModel(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    val inputType: DydxTriggerOrderInputType,
    private val triggerOrderStream: TriggerOrderStreaming,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTriggerSectionHeaderView.ViewState?> =
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
    ): DydxTriggerSectionHeaderView.ViewState {
        val marketConfigs = configsAndAsset?.configs
        val tickSize = marketConfigs?.displayTickSizeDecimals ?: 0

        fun formatOrder(orderPrice: TriggerPrice) =
            when (displayType) {
                // showing the reverse of the display type
                GainLossDisplayType.Percent -> orderPrice.usdcDiff?.let { diff ->
                    SignedAmountView.ViewState(
                        text = formatter.dollar(kotlin.math.abs(diff), tickSize),
                        sign = if (inputType == DydxTriggerOrderInputType.TakeProfit) PlatformUISign.Plus else PlatformUISign.Minus,
                        coloringOption = SignedAmountView.ColoringOption.SignOnly,
                    )
                }
                GainLossDisplayType.Amount -> orderPrice.percentDiff?.let { diff ->
                    SignedAmountView.ViewState(
                        text = formatter.percent(kotlin.math.abs(diff / 100.0), 2),
                        sign = if (inputType == DydxTriggerOrderInputType.TakeProfit) PlatformUISign.Plus else PlatformUISign.Minus,
                        coloringOption = SignedAmountView.ColoringOption.SignOnly,
                    )
                }
            }

        return DydxTriggerSectionHeaderView.ViewState(
            localizer = localizer,
            label = when (inputType) {
                DydxTriggerOrderInputType.TakeProfit -> localizer.localize("APP.GENERAL.GAIN")
                DydxTriggerOrderInputType.StopLoss -> localizer.localize("APP.GENERAL.LOSS")
            },
            amount = when (inputType) {
                DydxTriggerOrderInputType.TakeProfit -> triggerOrdersInput?.takeProfitOrder?.price?.let {
                    formatOrder(it)
                }
                DydxTriggerOrderInputType.StopLoss -> triggerOrdersInput?.stopLossOrder?.price?.let {
                    formatOrder(it)
                }
            },
            clearAction = {
                when (inputType) {
                    DydxTriggerOrderInputType.TakeProfit -> {
                        val fields = listOf(
                            TriggerOrdersInputField.takeProfitOrderType,
                            TriggerOrdersInputField.takeProfitUsdcDiff,
                            TriggerOrdersInputField.takeProfitPercentDiff,
                            TriggerOrdersInputField.takeProfitPrice,
                            TriggerOrdersInputField.takeProfitOrderSize,
                            TriggerOrdersInputField.takeProfitLimitPrice,
                        )
                        fields.forEach { abacusStateManager.triggerOrders(null, it) }
                    }
                    DydxTriggerOrderInputType.StopLoss -> {
                        val fields = listOf(
                            TriggerOrdersInputField.stopLossOrderType,
                            TriggerOrdersInputField.stopLossUsdcDiff,
                            TriggerOrdersInputField.stopLossPercentDiff,
                            TriggerOrdersInputField.stopLossPrice,
                            TriggerOrdersInputField.stopLossOrderSize,
                            TriggerOrdersInputField.stopLossLimitPrice,
                        )
                        fields.forEach { abacusStateManager.triggerOrders(null, it) }
                    }
                }
            },
        )
    }
}
