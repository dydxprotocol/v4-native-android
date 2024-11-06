package exchange.dydx.trading.feature.trade.trigger.components.inputfields.size

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.SubaccountPosition
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.platformui.components.inputs.PlatformInputAlertState
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LabeledTextInput
import exchange.dydx.trading.feature.trade.alertState
import exchange.dydx.trading.feature.trade.streams.MutableTriggerOrderStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class DydxTriggerOrderSizeViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val triggerOrderStream: MutableTriggerOrderStreaming,
) : ViewModel(), DydxViewModel {

    private val enabledFlow = MutableStateFlow(false)

    val state: Flow<DydxTriggerOrderSizeView.ViewState?> =
        combine(
            enabledFlow,
            abacusStateManager.state.triggerOrdersInput
                .mapNotNull { it?.marketId }
                .flatMapLatest {
                    abacusStateManager.state.selectedSubaccountPositionOfMarket(it)
                }
                .filterNotNull(),
            abacusStateManager.state.triggerOrdersInput,
            abacusStateManager.state.configsAndAssetMap,
            abacusStateManager.state.validationErrors,
        ) { sizeEnabled, position, triggerOrdersInput, configsAndAssetMap, validationErrors ->
            val marketId = triggerOrdersInput?.marketId ?: return@combine null
            createViewState(sizeEnabled, position, triggerOrdersInput, configsAndAssetMap?.get(marketId), validationErrors)
        }
            .distinctUntilChanged()

    private fun createViewState(
        sizeEnabled: Boolean,
        position: SubaccountPosition,
        triggerOrdersInput: TriggerOrdersInput?,
        configsAndAsset: MarketConfigsAndAsset?,
        validationErrors: List<ValidationError>?,
    ): DydxTriggerOrderSizeView.ViewState {
        val marketConfigs = configsAndAsset?.configs
        val stepSize = marketConfigs?.stepSize
        val size = abs(triggerOrdersInput?.size ?: 0.0)
        val positionSize = abs(position.size.current ?: 0.0)
        val percentage = if (positionSize != 0.0) {
            size / positionSize
        } else {
            0.0
        }
        val firstErrorOrWarning = validationErrors?.firstOrNull { it.type == ErrorType.error }
            ?: validationErrors?.firstOrNull { it.type == ErrorType.warning }

        return DydxTriggerOrderSizeView.ViewState(
            localizer = localizer,
            enabled = sizeEnabled,
            onEnabledChanged = { enabled ->
                enabledFlow.value = enabled
                if (!enabled) {
                    abacusStateManager.triggerOrders(
                        formatter.decimalLocaleAgnostic(abs(position.size.current ?: 0.0), size = stepSize),
                        TriggerOrdersInputField.size,
                    )
                }
            },
            labeledTextInput = LabeledTextInput.ViewState(
                localizer = localizer,
                label = localizer.localize("APP.GENERAL.AMOUNT"),
                token = configsAndAsset?.asset?.displayableAssetId,
                value = formatter.decimalLocaleAgnostic(size, size = stepSize),
                alertState = if (firstErrorOrWarning?.fields?.contains(TriggerOrdersInputField.size.rawValue) == true) {
                    firstErrorOrWarning.alertState
                } else {
                    PlatformInputAlertState.None
                },
                placeholder = formatter.raw(0.0, size = stepSize),
                onValueChanged = { value ->
                    abacusStateManager.triggerOrders(value, TriggerOrdersInputField.size)
                },
            ),
            percentage = percentage,
            onPercentageChanged = { percentage ->
                abacusStateManager.triggerOrders(
                    formatter.decimalLocaleAgnostic(positionSize * percentage, size = stepSize),
                    TriggerOrdersInputField.size,
                )
            },
            canEdit = true,
        )
    }
}
