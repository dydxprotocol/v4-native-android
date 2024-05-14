package exchange.dydx.trading.feature.trade.tradeinput

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.PlatformInfo
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTradeInputMarginModeViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    val platformInfo: PlatformInfo,
) : ViewModel(), DydxViewModel {
    val state: Flow<DydxTradeInputMarginModeView.ViewState?> =
        abacusStateManager.state.tradeInput
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(tradeInput: TradeInput?): DydxTradeInputMarginModeView.ViewState {
        val marginMode = tradeInput?.marginMode ?: MarginMode.cross
        return DydxTradeInputMarginModeView.ViewState(
            title = localizer.localize("APP.GENERAL.MARGIN_MODE"),
            asset = tradeInput?.marketId ?: "",
            crossMargin = DydxTradeInputMarginModeView.MarginTypeSelection(
                localizer.localize("APP.GENERAL.CROSS_MARGIN"),
                localizer.localize("APP.GENERAL.CROSS_MARGIN_DESCRIPTION"),
                marginMode == MarginMode.cross,
            ) {
                abacusStateManager.trade("CROSS", TradeInputField.marginMode)
                router.navigateBack()
            },
            isolatedMargin = DydxTradeInputMarginModeView.MarginTypeSelection(
                localizer.localize("APP.GENERAL.ISOLATED_MARGIN"),
                localizer.localize("APP.GENERAL.ISOLATED_MARGIN_DESCRIPTION"),
                marginMode == MarginMode.isolated,
            ) {
                abacusStateManager.trade("ISOLATED", TradeInputField.marginMode)
                router.navigateBack()
            },
            errorText = null,
            closeAction = {
                router.navigateBack()
            },
        )
    }
}
