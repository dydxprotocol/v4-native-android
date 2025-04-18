package exchange.dydx.trading.feature.trade.trigger.components.inputfields.limitprice

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.TriggerOrdersInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTriggerOrderLimitPriceSectionViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    private val enabledFlow = MutableStateFlow(false)

    val state: Flow<DydxTriggerOrderLimitPriceSectionView.ViewState?> =
        enabledFlow
            .map { sizeEnabled -> createViewState(sizeEnabled) }
            .distinctUntilChanged()

    private fun createViewState(
        sizeEnabled: Boolean,
    ): DydxTriggerOrderLimitPriceSectionView.ViewState {
        return DydxTriggerOrderLimitPriceSectionView.ViewState(
            localizer = localizer,
            enabled = sizeEnabled,
            onEnabledChanged = { enabled ->
                enabledFlow.value = enabled
                if (!enabled) {
                    abacusStateManager.triggerOrders(
                        null,
                        TriggerOrdersInputField.takeProfitLimitPrice,
                    )
                    abacusStateManager.triggerOrders(
                        null,
                        TriggerOrdersInputField.stopLossLimitPrice,
                    )
                    abacusStateManager.triggerOrders(
                        OrderType.TakeProfitMarket.rawValue,
                        TriggerOrdersInputField.takeProfitOrderType,
                    )
                    abacusStateManager.triggerOrders(
                        OrderType.StopMarket.rawValue,
                        TriggerOrdersInputField.stopLossOrderType,
                    )
                } else {
                    abacusStateManager.triggerOrders(
                        OrderType.TakeProfitLimit.rawValue,
                        TriggerOrdersInputField.takeProfitOrderType,
                    )
                    abacusStateManager.triggerOrders(
                        OrderType.StopLimit.rawValue,
                        TriggerOrdersInputField.stopLossOrderType,
                    )
                }
            },
        )
    }
}
