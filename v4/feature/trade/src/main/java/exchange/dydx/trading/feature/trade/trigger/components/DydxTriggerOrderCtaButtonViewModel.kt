package exchange.dydx.trading.feature.trade.trigger.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.trade.streams.MutableTradeStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTriggerOrderCtaButtonViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val tradeStream: MutableTradeStreaming,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTriggerOrderCtaButtonView.ViewState?> =
        abacusStateManager.state.triggerOrdersInput
            .map { createViewState(it) }
            .distinctUntilChanged()

    private fun createViewState(
        triggerOrdersInput: TriggerOrdersInput?,
    ): DydxTriggerOrderCtaButtonView.ViewState {
        val takeProfitOrderSummary = triggerOrdersInput?.takeProfitOrder?.summary
        val stopLossOrderSummary = triggerOrdersInput?.stopLossOrder?.summary
        return DydxTriggerOrderCtaButtonView.ViewState(
            localizer = localizer,
            ctaButtonState = if ((triggerOrdersInput?.takeProfitOrder?.price != null && (takeProfitOrderSummary?.size ?: 0.0) > 0) ||
                (triggerOrdersInput?.stopLossOrder?.price != null && (stopLossOrderSummary?.size ?: 0.0) > 0)
            ) {
                DydxTriggerOrderCtaButtonView.State.Enabled()
            } else {
                DydxTriggerOrderCtaButtonView.State.Disabled()
            },
            ctaAction = {
                tradeStream.submitTriggerOrders()
            },
        )
    }
}
