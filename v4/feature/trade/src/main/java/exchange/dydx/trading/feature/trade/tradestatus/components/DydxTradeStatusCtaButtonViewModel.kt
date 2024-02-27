package exchange.dydx.trading.feature.trade.tradestatus.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTradeStatusCtaButtonViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeStatusCtaButtonView.ViewState?> =
        abacusStateManager.state.marketSummary
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(marketSummary: PerpetualMarketSummary?): DydxTradeStatusCtaButtonView.ViewState {
        return DydxTradeStatusCtaButtonView.ViewState(
            localizer = localizer,
            returnAction = {
                router.navigateBack()
            },
        )
    }
}
