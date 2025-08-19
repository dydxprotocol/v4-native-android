package exchange.dydx.trading.feature.portfolio.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.DydxBoolFeatureFlag
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.common.navigation.TransferRoutes
import exchange.dydx.trading.integration.react.TurnkeyReactBridge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxPortfolioHeaderViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val turnkeyBridge: TurnkeyReactBridge,
    private val featureFlags: DydxFeatureFlags,
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
                // turnkeyBridge.testFunction()
                router.navigateTo(
                    route = OnboardingRoutes.welcome,
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
            depositAction = {
                if (featureFlags.isFeatureEnabled(DydxBoolFeatureFlag.ff_turnkey_android)) {
                    router.navigateTo(
                        route = TransferRoutes.transfer_selector,
                        presentation = DydxRouter.Presentation.Modal,
                    )
                } else {
                    router.navigateTo(
                        route = TransferRoutes.transfer,
                        presentation = DydxRouter.Presentation.Modal,
                    )
                }
            },
        )
    }
}
