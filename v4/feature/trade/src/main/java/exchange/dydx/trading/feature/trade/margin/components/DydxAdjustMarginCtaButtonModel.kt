package exchange.dydx.trading.feature.trade.margin.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.theme.DydxTheme
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxAdjustMarginCtaButtonModel @Inject constructor(
    private val appConfig: AppConfig,
    private val theme: DydxTheme,
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {
    val state: Flow<DydxAdjustMarginCtaButton.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput,
            abacusStateManager.state.validationErrors,
        ) { tradeInput, validationErrors ->
            createViewState(tradeInput, validationErrors)
        }
            .distinctUntilChanged()

    private fun createViewState(
        tradeInput: TradeInput?,
        validationErrors: List<ValidationError>,
    ): DydxAdjustMarginCtaButton.ViewState {
        val firstBlockingError =
            validationErrors.firstOrNull { it.type == ErrorType.required || it.type == ErrorType.error }

        return DydxAdjustMarginCtaButton.ViewState(
            ctaButton = InputCtaButton.ViewState(
                localizer = localizer,
                ctaButtonState = InputCtaButton.State.Enabled(
                    localizer.localize("APP.TRADE.ADD_MARGIN"),
                ),
                ctaAction = {
                    // TODO, Submit the orders
                },
            ),
        )
    }
}
