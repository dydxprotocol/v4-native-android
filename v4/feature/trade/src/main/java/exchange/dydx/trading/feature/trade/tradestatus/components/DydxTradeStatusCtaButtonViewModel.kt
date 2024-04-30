package exchange.dydx.trading.feature.trade.tradestatus.components

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.trade.streams.MutableTradeStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTradeStatusCtaButtonViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
    private val tradeStream: MutableTradeStreaming,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel(), DydxViewModel {

    private enum class TradeType {
        Trade,
        ClosePosition;

        companion object {
            fun fromString(value: String?): TradeType {
                return when (value) {
                    "trade" -> Trade
                    "closePosition" -> ClosePosition
                    else -> throw IllegalArgumentException("Invalid trade type: $value")
                }
            }
        }
    }

    private val tradeType = TradeType.fromString(savedStateHandle["tradeType"])

    val state: Flow<DydxTradeStatusCtaButtonView.ViewState?> =
        tradeStream.submissionStatus
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        submissionStatus: AbacusStateManagerProtocol.SubmissionStatus?,
    ): DydxTradeStatusCtaButtonView.ViewState {
        return when (submissionStatus) {
            is AbacusStateManagerProtocol.SubmissionStatus.Success ->
                DydxTradeStatusCtaButtonView.ViewState(
                    localizer = localizer,
                    ctaButtonTitle = localizer.localize("APP.TRADE.RETURN_TO_MARKET"),
                    ctaButtonState = PlatformButtonState.Secondary,
                    ctaButtonAction = {
                        router.navigateBack()
                    },
                )
            is AbacusStateManagerProtocol.SubmissionStatus.Failed ->
                DydxTradeStatusCtaButtonView.ViewState(
                    localizer = localizer,
                    ctaButtonTitle = localizer.localize("APP.ONBOARDING.TRY_AGAIN"),
                    ctaButtonState = PlatformButtonState.Primary,
                    ctaButtonAction = {
                        when (tradeType) {
                            TradeType.Trade -> tradeStream.submitTrade()
                            TradeType.ClosePosition -> tradeStream.closePosition()
                        }
                    },
                )
            else ->
                DydxTradeStatusCtaButtonView.ViewState(
                    localizer = localizer,
                    ctaButtonTitle = localizer.localize("APP.TRADE.SUBMITTING_ORDER"),
                    ctaButtonState = PlatformButtonState.Disabled,
                    ctaButtonAction = {},
                )
        }
    }
}
