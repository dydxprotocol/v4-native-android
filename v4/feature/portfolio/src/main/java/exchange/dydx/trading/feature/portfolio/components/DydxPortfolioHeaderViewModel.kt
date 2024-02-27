package exchange.dydx.trading.feature.portfolio.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.common.navigation.TransferRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxPortfolioHeaderViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxPortfolioHeaderView.ViewState?> = abacusStateManager.state.onboarded
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(onboarded: Boolean): DydxPortfolioHeaderView.ViewState {
        return DydxPortfolioHeaderView.ViewState(
            localizer = localizer,
            state = if (onboarded) DydxPortfolioHeaderView.OnboardState.Onboarded else DydxPortfolioHeaderView.OnboardState.NotOnboarded,
            onboardAction = {
                router.navigateTo(
                    route = OnboardingRoutes.welcome,
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
            depositAction = {
                router.navigateTo(
                    route = TransferRoutes.transfer,
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
        )
    }
}
