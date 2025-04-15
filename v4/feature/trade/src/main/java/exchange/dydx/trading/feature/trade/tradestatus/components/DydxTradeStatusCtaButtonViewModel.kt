package exchange.dydx.trading.feature.trade.tradestatus.components

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.PlatformDialog
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.trade.streams.MutableTradeStreaming
import exchange.dydx.trading.integration.fcm.PRIMER_SHOWN_KEY
import exchange.dydx.trading.integration.fcm.PushPermissionRequesterProtocol
import exchange.dydx.utilities.utils.SharedPreferencesStore
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
    private val pushPermissionRequester: PushPermissionRequesterProtocol,
    private val notificationPrimerDialog: PlatformDialog,
    private val sharedPreferencesStore: SharedPreferencesStore,
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
                        if (pushPermissionRequester.shouldRequestPermission) {
                            notificationPrimerDialog.showMessage(
                                title = localizer.localize("APP.PUSH_NOTIFICATIONS.PRIMER_TITLE"),
                                message = localizer.localize("APP.PUSH_NOTIFICATIONS.PRIMER_MESSAGE"),
                                cancelTitle = localizer.localize("APP.GENERAL.NOT_NOW"),
                                confirmTitle = localizer.localize("APP.GENERAL.OK"),
                                confirmAction = {
                                    router.navigateBack()
                                    pushPermissionRequester.requestPushPermission()
                                },
                                cancelAction = {
                                    router.navigateBack()
                                    sharedPreferencesStore.save("true", PRIMER_SHOWN_KEY)
                                },
                            )
                        } else {
                            router.navigateBack()
                        }
                    },
                    notificationPrimerDialog = notificationPrimerDialog,
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
                    notificationPrimerDialog = notificationPrimerDialog,
                )
            else ->
                DydxTradeStatusCtaButtonView.ViewState(
                    localizer = localizer,
                    ctaButtonTitle = localizer.localize("APP.TRADE.SUBMITTING_ORDER"),
                    ctaButtonState = PlatformButtonState.Disabled,
                    ctaButtonAction = {},
                    notificationPrimerDialog = notificationPrimerDialog,
                )
        }
    }
}
