package exchange.dydx.trading.feature.trade.closeposition.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.ClosePositionInput
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.TradeRoutes
import exchange.dydx.trading.feature.trade.streams.MutableTradeStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class DydxClosePositionInputCtaButtonViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val tradeStream: MutableTradeStreaming,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxClosePositionInputCtaButtonView.ViewState?> =
        combine(
            abacusStateManager.state.closePositionInput,
            abacusStateManager.state.validationErrors,
        ) { tradeInput, validationErrors ->
            createViewState(tradeInput, validationErrors)
        }

    private fun createViewState(
        input: ClosePositionInput?,
        validationErrors: List<ValidationError>,
    ): DydxClosePositionInputCtaButtonView.ViewState {
        val firstBlockingError = validationErrors.firstOrNull { it.type == ErrorType.required || it.type == ErrorType.error }

        return DydxClosePositionInputCtaButtonView.ViewState(
            localizer = localizer,
            ctaButtonState = when {
                firstBlockingError != null -> DydxClosePositionInputCtaButtonView.State.Disabled(
                    firstBlockingError.resources.action?.localizedString(localizer),
                )
                (input?.size?.size ?: 0.0) > 0.0 ->
                    if (firstBlockingError != null) {
                        DydxClosePositionInputCtaButtonView.State.Disabled(
                            firstBlockingError.resources.action?.localizedString(localizer),
                        )
                    } else {
                        DydxClosePositionInputCtaButtonView.State.Enabled(
                            localizer.localize("APP.TRADE.CLOSE_POSITION"),
                        )
                    }
                else -> DydxClosePositionInputCtaButtonView.State.Disabled()
            },
            ctaAction = {
                tradeStream.closePosition()
                router.navigateBack()
                router.navigateTo(
                    route = TradeRoutes.status + "/closePosition",
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
        )
    }
}
