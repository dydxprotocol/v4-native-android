package exchange.dydx.trading.feature.trade.trigger.components.inputfields.size

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.SubaccountPosition
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LabeledTextInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class DydxTriggerOrderSizeViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    private val enabledFlow = MutableStateFlow(false)
    private val marketIdFlow = abacusStateManager.state.triggerOrdersInput
        .mapNotNull { it?.marketId }

    val state: Flow<DydxTriggerOrderSizeView.ViewState?> =
        combine(
            enabledFlow,
            marketIdFlow
                .flatMapLatest { marketId ->
                    abacusStateManager.state.selectedSubaccountPositionOfMarket(marketId)
                }
                .filterNotNull()
                .distinctUntilChanged(),
            abacusStateManager.state.triggerOrdersInput,
            abacusStateManager.state.configsAndAssetMap,
        ) { sizeEnabled, position, triggerOrdersInput, configsAndAssetMap ->
            val marketId = triggerOrdersInput?.marketId ?: return@combine null
            createViewState(sizeEnabled, position, triggerOrdersInput, configsAndAssetMap?.get(marketId))
        }
            .distinctUntilChanged()

    private fun createViewState(
        sizeEnabled: Boolean,
        position: SubaccountPosition,
        triggerOrdersInput: TriggerOrdersInput?,
        configsAndAsset: MarketConfigsAndAsset?,
    ): DydxTriggerOrderSizeView.ViewState {
        val marketConfigs = configsAndAsset?.configs
        val stepSize = marketConfigs?.displayStepSizeDecimals ?: 0
        val size = triggerOrdersInput?.size ?: 0.0
        val positionSize = position.size?.current ?: 0.0
        val percentage = if (positionSize > 0.0) {
            size / positionSize
        } else {
            0.0
        }
        return DydxTriggerOrderSizeView.ViewState(
            localizer = localizer,
            enabled = sizeEnabled,
            onEnabledChanged = { enabled ->
                enabledFlow.value = enabled
                if (!enabled) {
                    abacusStateManager.triggerOrders(
                        formatter.decimalLocaleAgnostic(position.size?.current),
                        TriggerOrdersInputField.size,
                    )
                }
            },
            labeledTextInput = LabeledTextInput.ViewState(
                localizer = localizer,
                label = localizer.localize("APP.GENERAL.AMOUNT"),
                token = configsAndAsset?.asset?.id,
                value = formatter.decimalLocaleAgnostic(size, stepSize),
                placeholder = formatter.raw(0.0, stepSize),
                onValueChanged = { value ->
                    abacusStateManager.triggerOrders(value, TriggerOrdersInputField.size)
                },
            ),
            percentage = percentage,
            onPercentageChanged = { percentage ->
                abacusStateManager.triggerOrders(
                    formatter.decimalLocaleAgnostic(positionSize * percentage),
                    TriggerOrdersInputField.size,
                )
            },
        )
    }
}
