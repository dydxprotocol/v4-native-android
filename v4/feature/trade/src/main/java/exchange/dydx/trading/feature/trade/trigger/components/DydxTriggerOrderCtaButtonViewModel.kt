package exchange.dydx.trading.feature.trade.trigger.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.trade.streams.MutableTriggerOrderStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTriggerOrderCtaButtonViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val triggerOrderStream: MutableTriggerOrderStreaming,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    private val pendingSubmissionFlow: MutableStateFlow<Int> = MutableStateFlow(0)

    val state: Flow<DydxTriggerOrderCtaButtonView.ViewState?> =
        combine(
            triggerOrderStream.isNewTriggerOrder,
            abacusStateManager.state.triggerOrdersInput,
            abacusStateManager.state.validationErrors,
            pendingSubmissionFlow,
        ) { isNewTriggerOrder, triggerOrdersInput, error, pendingSubmission ->
            createViewState(isNewTriggerOrder, triggerOrdersInput, error, pendingSubmission)
        }
            .distinctUntilChanged()

    private fun createViewState(
        isNewTriggerOrder: Boolean,
        triggerOrdersInput: TriggerOrdersInput?,
        errors: List<ValidationError>,
        pendingSubmission: Int,
    ): DydxTriggerOrderCtaButtonView.ViewState {
        val isSubmitting = pendingSubmission > 0
        val firstBlockingError =
            errors.firstOrNull { it.type == ErrorType.required || it.type == ErrorType.error }
        val buttonTitle = if (isSubmitting) {
            localizer.localize("APP.TRADE.SUBMITTING_ORDER")
        } else {
            firstBlockingError?.resources?.action?.localized
                ?: if (isNewTriggerOrder) {
                    localizer.localize("APP.TRADE.ADD_TRIGGERS")
                } else {
                    localizer.localize("APP.TRADE.UPDATE_TRIGGERS")
                }
        }
        val inputSize = triggerOrdersInput?.size ?: 0.0
        val tpSize = triggerOrdersInput?.takeProfitOrder?.size ?: 0.0
        val slSize = triggerOrdersInput?.stopLossOrder?.size ?: 0.0
        val hasSize = inputSize != 0.0 || tpSize != 0.0 || slSize != 0.0
        return DydxTriggerOrderCtaButtonView.ViewState(
            localizer = localizer,
            ctaButtonState = if (isSubmitting) {
                DydxTriggerOrderCtaButtonView.State.Disabled(buttonTitle)
            } else if (
                (
                    triggerOrdersInput?.takeProfitOrder?.price?.triggerPrice != null || triggerOrdersInput?.takeProfitOrder?.orderId != null ||
                        triggerOrdersInput?.stopLossOrder?.price?.triggerPrice != null || triggerOrdersInput?.stopLossOrder?.orderId != null
                    ) &&
                hasSize &&
                firstBlockingError == null
            ) {
                DydxTriggerOrderCtaButtonView.State.Enabled(buttonTitle)
            } else {
                DydxTriggerOrderCtaButtonView.State.Disabled(buttonTitle)
            },
            ctaAction = {
                pendingSubmissionFlow.value = abacusStateManager.commitTriggerOrders { _ ->
                    // order status will be shown from PresentationProtocol.showToast()
                    pendingSubmissionFlow.value -= 1
                    if (pendingSubmissionFlow.value == 0) {
                        router.navigateBack()
                    }
                }
            },
        )
    }
}
