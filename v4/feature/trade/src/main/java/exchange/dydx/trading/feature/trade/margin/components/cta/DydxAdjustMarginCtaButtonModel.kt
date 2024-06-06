package exchange.dydx.trading.feature.trade.margin.components.cta

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInput
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.Toast
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@HiltViewModel
class DydxAdjustMarginCtaButtonModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val platformInfo: PlatformInfo,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {
    val state: Flow<DydxAdjustMarginCtaButton.ViewState?> =
        combine(
            abacusStateManager.state.adjustMarginInput.filterNotNull(),
            abacusStateManager.state.validationErrors,
        ) { adjustMarginInput, validationErrors ->
            createViewState(adjustMarginInput, validationErrors)
        }
            .distinctUntilChanged()

    private fun createViewState(
        adjustMarginInput: AdjustIsolatedMarginInput,
        validationErrors: List<ValidationError>,
    ): DydxAdjustMarginCtaButton.ViewState {
        val firstBlockingError =
            validationErrors.firstOrNull { it.type == ErrorType.required || it.type == ErrorType.error }

        return DydxAdjustMarginCtaButton.ViewState(
            ctaButton = InputCtaButton.ViewState(
                localizer = localizer,
                ctaButtonState =
                InputCtaButton.State.Enabled(
                    localizer.localize("APP.TRADE.ADD_MARGIN"),
                ),
                ctaAction = {
                    commitAdjustMargin()
                },
            ),
        )
    }

    private fun commitAdjustMargin() {
        abacusStateManager.commitAdjustIsolatedMargin { status ->
            when (status) {
                is AbacusStateManagerProtocol.SubmissionStatus.Success -> {
                    router.navigateBack()
                }
                is AbacusStateManagerProtocol.SubmissionStatus.Failed -> {
                    platformInfo.show(
                        title = localizer.localize("ERRORS.GENERAL.SOMETHING_WENT_WRONG"),
                        message = status.error?.message ?: "",
                        type = Toast.Type.Error,
                    )
                }
            }
        }
    }
}
