package exchange.dydx.trading.feature.trade.tradeinput.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.PlatformInfoViewModel
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.TradeRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTradeInputMarginButtonsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val platformInfo: PlatformInfo,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeInputMarginButtonsView.ViewState?> =
        abacusStateManager.state.tradeInput
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        tradeInput: TradeInput?,
    ): DydxTradeInputMarginButtonsView.ViewState {
        return DydxTradeInputMarginButtonsView.ViewState(
            localizer = localizer,
            isIsolatedMarketSelected = tradeInput?.marginMode == MarginMode.Isolated,
            isolatedMarketTargetLeverageText = formatter.leverage(
                (tradeInput?.targetLeverage ?: 2.0),
                1,
            ),
            onMarginType = {
                if (tradeInput?.options?.needsMarginMode == false) {
                    val market = tradeInput.marketId ?: ""
                    platformInfo.show(
                        message = localizer.localizeWithParams(
                            path = "WARNINGS.TRADE_BOX.UNABLE_TO_CHANGE_MARGIN_MODE",
                            params = mapOf(
                                "MARKET" to market,
                            ),
                        ),
                        type = PlatformInfoViewModel.Type.Warning,
                    )
                } else {
                    router.navigateTo(
                        route = TradeRoutes.margin_mode,
                        presentation = DydxRouter.Presentation.Modal,
                    )
                }
            },
            onTargetLeverage = {
                router.navigateTo(
                    route = TradeRoutes.target_leverage,
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
        )
    }
}
