package exchange.dydx.trading.feature.trade.trigger.components.inputfields.price

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LabeledTextInput
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
        ) { triggerOrdersInput, configsAndAssetMap ->
            val marketId = triggerOrdersInput?.marketId ?: return@combine null
            createViewState(triggerOrdersInput, configsAndAssetMap?.get(marketId))
        }
            .distinctUntilChanged()

    private fun createViewState(
        triggerOrdersInput: TriggerOrdersInput?,
        configsAndAsset: MarketConfigsAndAsset?,
    ): DydxTriggerOrderPriceView.ViewState {
        val marketConfigs = configsAndAsset?.configs
        val value = when (inputType) {
            DydxTriggerOrderPriceInputType.TakeProfit -> null
            DydxTriggerOrderPriceInputType.StopLoss -> null
            DydxTriggerOrderPriceInputType.TakeProfitLimit -> null
            DydxTriggerOrderPriceInputType.StopLossLimit -> null
        }
        val label = when (inputType) {
            DydxTriggerOrderPriceInputType.TakeProfit -> localizer.localize("APP.TRIGGERS_MODAL.TP_PRICE")
            DydxTriggerOrderPriceInputType.StopLoss -> localizer.localize("APP.TRIGGERS_MODAL.SL_PRICE")
            DydxTriggerOrderPriceInputType.TakeProfitLimit -> localizer.localize("APP.TRIGGERS_MODAL.TP_LIMIT")
            DydxTriggerOrderPriceInputType.StopLossLimit -> localizer.localize("APP.TRIGGERS_MODAL.SL_LIMIT")
        }
        return DydxTriggerOrderPriceView.ViewState(
            localizer = localizer,
            labeledTextInput = LabeledTextInput.ViewState(
                localizer = localizer,
                label = label,
                token = "USD",
                value = value,
                placeholder = formatter.raw(0.0, marketConfigs?.displayTickSizeDecimals ?: 0),
                onValueChanged = { value ->
                    //  abacusStateManager.trade(value, TradeInputField.limitPrice)
                },
            ),
        )
    }
}
