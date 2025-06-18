package exchange.dydx.trading.feature.portfolio.components.fundings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.output.account.SubaccountFundingPayment
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.PortfolioRoutes
import exchange.dydx.trading.feature.portfolio.components.fills.DydxPortfolioFillsView
import exchange.dydx.trading.feature.shared.viewstate.SharedFillViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxPortfolioFundingsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxPortfolioFundingsView.ViewState?> = combine(
        abacusStateManager.marketId,
        abacusStateManager.state.selectedSubaccountFundings,
    ) { marketId, fundings,  ->
        createViewState(marketId, fundings)
    }
        .distinctUntilChanged()

    private fun createViewState(
        marketId: String?,
        fundings: List<SubaccountFundingPayment>?,
    ): DydxPortfolioFundingsView.ViewState {
        val fundings = if (marketId != null) {
            fundings?.filter { it.marketId == marketId }
        } else {
            fundings
        }
        return DydxPortfolioFundingsView.ViewState(
            localizer = localizer,
        )
    }
}
