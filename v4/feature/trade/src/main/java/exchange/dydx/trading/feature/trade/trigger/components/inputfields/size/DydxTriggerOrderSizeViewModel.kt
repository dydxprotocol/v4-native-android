package exchange.dydx.trading.feature.trade.trigger.components.inputfields.size

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LabeledTextInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTriggerOrderSizeViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    private val enabled = MutableStateFlow(false)

    val state: Flow<DydxTriggerOrderSizeView.ViewState?> =
        combine(
            enabled,
            abacusStateManager.state.triggerOrdersInput,
            abacusStateManager.state.configsAndAssetMap,
        ) { sizeEnabled, triggerOrdersInput, configsAndAssetMap ->
            val marketId = triggerOrdersInput?.marketId ?: return@combine null
            createViewState(sizeEnabled, triggerOrdersInput, configsAndAssetMap?.get(marketId))
        }
            .distinctUntilChanged()

    private fun createViewState(
        sizeEnabled: Boolean,
        triggerOrdersInput: TriggerOrdersInput?,
        configsAndAsset: MarketConfigsAndAsset?,
    ): DydxTriggerOrderSizeView.ViewState {
        val marketConfigs = configsAndAsset?.configs
        return DydxTriggerOrderSizeView.ViewState(
            localizer = localizer,
            enabled = sizeEnabled,
            onEnabledChanged = { enabled.value = it },
            labeledTextInput = LabeledTextInput.ViewState(
                localizer = localizer,
                label = localizer.localize("APP.GENERAL.AMOUNT"),
                token = configsAndAsset?.asset?.name,
                value = null,
                placeholder = formatter.raw(0.0, marketConfigs?.displayStepSizeDecimals ?: 0),
                onValueChanged = { value ->
                    //  abacusStateManager.trade(value, TradeInputField.limitPrice)
                },
            ),
        )
    }
}
