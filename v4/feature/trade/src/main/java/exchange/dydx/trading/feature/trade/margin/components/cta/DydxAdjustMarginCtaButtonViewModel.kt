package exchange.dydx.trading.feature.trade.margin.components.cta

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInput
import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.PlatformInfoViewModel
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@HiltViewModel
class DydxAdjustMarginCtaButtonViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val platformInfo: PlatformInfo,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {
    private val isSubmittingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val state: Flow<DydxAdjustMarginCtaButton.ViewState?> =
        combine(
            abacusStateManager.state.adjustMarginInput.filterNotNull(),
            isSubmittingFlow,
        ) { adjustMarginInput, isSubmitting ->
            createViewState(adjustMarginInput, isSubmitting)
        }
            .distinctUntilChanged()

    private fun createViewState(
        adjustMarginInput: AdjustIsolatedMarginInput,
        isSubmitting: Boolean,
    ): DydxAdjustMarginCtaButton.ViewState {
        return DydxAdjustMarginCtaButton.ViewState(
            ctaButton = InputCtaButton.ViewState(
                localizer = localizer,
                ctaButtonState =
                if (isSubmitting) {
                    InputCtaButton.State.Disabled(
                        localizer.localize("APP.TRADE.SUBMITTING"),
                    )
                } else {
                    InputCtaButton.State.Enabled(
                        when (adjustMarginInput.type) {
                            IsolatedMarginAdjustmentType.Add -> {
                                localizer.localize("APP.TRADE.ADD_MARGIN")
                            }

                            IsolatedMarginAdjustmentType.Remove -> {
                                localizer.localize("APP.TRADE.REMOVE_MARGIN")
                            }
                        },
                    )
                },
                ctaAction = {
                    commitAdjustMargin()
                },
            ),
        )
    }

    private fun commitAdjustMargin() {
        isSubmittingFlow.value = true
        abacusStateManager.commitAdjustIsolatedMargin { status ->
            isSubmittingFlow.value = false
            when (status) {
                is AbacusStateManagerProtocol.SubmissionStatus.Success -> {
                    router.navigateBack()
                }
                is AbacusStateManagerProtocol.SubmissionStatus.Failed -> {
                    platformInfo.show(
                        title = localizer.localize("ERRORS.GENERAL.SOMETHING_WENT_WRONG"),
                        message = status.error?.message ?: "",
                        type = PlatformInfoViewModel.Type.Error,
                    )
                }
            }
        }
    }
}
