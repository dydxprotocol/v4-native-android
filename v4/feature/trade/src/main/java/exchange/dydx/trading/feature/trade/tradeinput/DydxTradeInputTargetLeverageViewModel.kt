package exchange.dydx.trading.feature.trade.tradeinput

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
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
class DydxTradeInputTargetLeverageViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    val platformInfo: PlatformInfo,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeInputTargetLeverageView.ViewState?> =
        abacusStateManager.state.tradeInput
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(tradeInput: TradeInput?): DydxTradeInputTargetLeverageView.ViewState {
        val targetLeverage = tradeInput?.targetLeverage ?: 1.0
        val maxLeverage = tradeInput?.options?.maxLeverage ?: 5.0
        val leverages = leverageOptions(maxLeverage)
        return DydxTradeInputTargetLeverageView.ViewState(
            localizer,
            formatter.localFormatted(targetLeverage, 1),
            leverages,
            {
            },
        )
    }

    private fun leverageOptions(max: Double): List<LeverageTextAndValue> {
        val leverages = mutableListOf<LeverageTextAndValue>()
        if (max > 1.0) {
            leverages.add(LeverageTextAndValue("1.0", 1.0))
        }
        if (max > 2.0) {
            leverages.add(LeverageTextAndValue("2.0", 2.0))
        }
        if (max > 5.0) {
            leverages.add(LeverageTextAndValue("5.0", 2.0))
        }
        if (max > 10.0) {
            leverages.add(LeverageTextAndValue("10.0", 2.0))
        }
        leverages.add(LeverageTextAndValue(localizer.localize("APP.GENERAL.MAX"), max))
        return leverages
    }
}
