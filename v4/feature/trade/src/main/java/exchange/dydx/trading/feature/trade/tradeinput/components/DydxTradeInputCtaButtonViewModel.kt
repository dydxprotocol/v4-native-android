package exchange.dydx.trading.feature.trade.tradeinput.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.TradeRoutes
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import exchange.dydx.trading.feature.trade.streams.MutableTradeStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTradeInputCtaButtonViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val tradeStream: MutableTradeStreaming,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeInputCtaButtonView.ViewState?> =
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
    ): DydxTradeInputCtaButtonView.ViewState {
        val firstBlockingError =
            validationErrors.firstOrNull { it.type == ErrorType.required || it.type == ErrorType.error }

        return DydxTradeInputCtaButtonView.ViewState(
            ctaButton = InputCtaButton.ViewState(
                localizer = localizer,
                ctaButtonState = when {
                    firstBlockingError != null -> InputCtaButton.State.Disabled(
                        firstBlockingError.resources.action?.localizedString(localizer),
                    )

                    (tradeInput?.size?.size ?: 0.0) > 0.0 ->
                        if (firstBlockingError != null) {
                            InputCtaButton.State.Disabled(
                                firstBlockingError.resources.action?.localizedString(localizer),
                            )
                        } else {
                            val key: String = when (tradeInput?.type) {
                                OrderType.Market -> "APP.TRADE.PLACE_MARKET_ORDER"
                                OrderType.Limit -> "APP.TRADE.PLACE_LIMIT_ORDER"
                                OrderType.StopLimit -> "APP.TRADE.PLACE_STOP_LIMIT_ORDER"
                                OrderType.StopMarket -> "APP.TRADE.PLACE_STOP_MARKET_ORDER"
                                OrderType.TakeProfitLimit -> "APP.TRADE.PLACE_TAKE_PROFIT_LIMIT_ORDER"
                                OrderType.TakeProfitMarket -> "APP.TRADE.PLACE_TAKE_PROFIT_MARKET_ORDER"
                                OrderType.TrailingStop -> "APP.TRADE.PLACE_TRAILING_STOP_ORDER"
                                else -> "APP.TRADE.PLACE_TRADE"
                            }
                            InputCtaButton.State.Enabled(
                                localizer.localize(key),
                            )
                        }

                    else -> InputCtaButton.State.Disabled()
                },
                ctaAction = {
                    tradeStream.submitTrade()
                    router.navigateTo(
                        route = TradeRoutes.status + "/trade",
                        presentation = DydxRouter.Presentation.Modal,
                    )
                },
            ),
        )
    }
}
