package exchange.dydx.trading.feature.profile.rewards

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxRewardsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxRewardsView.ViewState?> = abacusStateManager.state.marketSummary
        .map {
            createViewState()
        }
        .distinctUntilChanged()

    private fun createViewState(): DydxRewardsView.ViewState {
        return DydxRewardsView.ViewState(
            localizer = localizer,
            backButtonAction = {
                router.navigateBack()
            },
        )
    }
}
