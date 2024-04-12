package exchange.dydx.trading.feature.trade.trigger.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.trade.streams.MutableTriggerOrderStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTriggerOrderCtaButtonViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val triggerOrderStream: MutableTriggerOrderStreaming,

) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTriggerOrderCtaButtonView.ViewState?> =
        combine(
            triggerOrderStream.isNewTriggerOrder,
            abacusStateManager.state.triggerOrdersInput,
        ) { isNewTriggerOrder, triggerOrdersInput ->
            createViewState(isNewTriggerOrder, triggerOrdersInput)
        }
            .distinctUntilChanged()

    private fun createViewState(
        isNewTriggerOrder: Boolean,
        triggerOrdersInput: TriggerOrdersInput?,
    ): DydxTriggerOrderCtaButtonView.ViewState {
        val buttonTitle = if (isNewTriggerOrder) {
            localizer.localize("APP.TRADE.ADD_TRIGGERS")
        } else {
            localizer.localize("APP.TRADE.EDIT_TRIGGERS")
        }
        return DydxTriggerOrderCtaButtonView.ViewState(
            localizer = localizer,
            ctaButtonState = if (
                (
                    triggerOrdersInput?.takeProfitOrder?.price?.triggerPrice != null || triggerOrdersInput?.takeProfitOrder?.orderId != null ||
                        triggerOrdersInput?.stopLossOrder?.price?.triggerPrice != null || triggerOrdersInput?.stopLossOrder?.orderId != null
                    ) &&
                triggerOrdersInput?.size ?: 0.0 > 0.0
            ) {
                DydxTriggerOrderCtaButtonView.State.Enabled(buttonTitle)
            } else {
                DydxTriggerOrderCtaButtonView.State.Disabled(buttonTitle)
            },
            ctaAction = {
                triggerOrderStream.updatesubmissionStatus(null)
                abacusStateManager.commitTriggerOrders { status ->
                    triggerOrderStream.updatesubmissionStatus(status)
                }
            },
        )
    }
}
