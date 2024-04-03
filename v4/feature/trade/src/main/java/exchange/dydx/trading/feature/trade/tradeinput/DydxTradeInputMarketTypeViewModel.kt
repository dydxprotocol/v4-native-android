package exchange.dydx.trading.feature.trade.tradeinput

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.PlatformInfo
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.theme.DydxTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTradeInputMarketTypeViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    val platformInfo: PlatformInfo,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeInputMarketTypeView.ViewState?> =
        abacusStateManager.state.marketSummary
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(marketSummary: PerpetualMarketSummary?): DydxTradeInputMarketTypeView.ViewState {
        val volume = formatter.dollarVolume(marketSummary?.volume24HUSDC)
        return DydxTradeInputMarketTypeView.ViewState(
            text = volume,
        )
    }
}
