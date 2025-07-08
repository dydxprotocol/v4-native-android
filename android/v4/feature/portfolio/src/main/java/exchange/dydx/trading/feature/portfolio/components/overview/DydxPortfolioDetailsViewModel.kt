package exchange.dydx.trading.feature.portfolio.components.overview

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.viewstate.SharedAccountViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxPortfolioDetailsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxPortfolioDetailsView.ViewState?> = abacusStateManager.state.selectedSubaccount
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(subaccount: Subaccount?): DydxPortfolioDetailsView.ViewState {
        return DydxPortfolioDetailsView.ViewState(
            localizer = localizer,
            sharedAccountViewModel = SharedAccountViewState.create(
                subaccount = subaccount,
                localizer = localizer,
                formatter = formatter,
            ),
            formatter = formatter,
        )
    }
}
