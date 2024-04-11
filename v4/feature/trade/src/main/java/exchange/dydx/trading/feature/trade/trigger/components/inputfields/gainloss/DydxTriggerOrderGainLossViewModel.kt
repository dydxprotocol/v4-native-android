package exchange.dydx.trading.feature.trade.trigger.components.inputfields.gainloss

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LabeledSelectionInput
import exchange.dydx.trading.feature.shared.views.LabeledTextInput
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

    val state: Flow<DydxTriggerOrderGainLossView.ViewState?> =
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
    ): DydxTriggerOrderGainLossView.ViewState {
        val label = when (inputType) {
            DydxTriggerOrderInputType.TakeProfit -> localizer.localize("APP.GENERAL.GAIN")
            DydxTriggerOrderInputType.StopLoss -> localizer.localize("APP.GENERAL.LOSS")
        }
        return DydxTriggerOrderGainLossView.ViewState(
            localizer = localizer,
            labeledTextInput = LabeledTextInput.ViewState(
                localizer = localizer,
                label = label,
                value = null,
                onValueChanged = { value ->
                    abacusStateManager.trade(value, TradeInputField.goodTilDuration)
                },
            ),
            labeledSelectionInput = LabeledSelectionInput.ViewState(
                localizer = localizer,
                options = listOf("%", "$"),
                selectedIndex = 0,
                onSelectionChanged = { index ->
//                    val type = tradeInput?.options?.goodTilUnitOptions?.getOrNull(index)?.type
//                    if (type != null) {
//                        abacusStateManager.trade(type, TradeInputField.goodTilUnit)
//                    }
                },
            ),
        )
    }
}
