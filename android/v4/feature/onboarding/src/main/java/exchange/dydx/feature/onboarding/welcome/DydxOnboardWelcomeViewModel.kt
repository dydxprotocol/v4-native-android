package exchange.dydx.feature.onboarding.welcome

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.feature.shared.analytics.OnboardingAnalytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxOnboardWelcomeViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val onboardingAnalytics: OnboardingAnalytics,
    private val featureFlags: DydxFeatureFlags,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxOnboardWelcomeView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxOnboardWelcomeView.ViewState {
        return DydxOnboardWelcomeView.ViewState(
            localizer = localizer,
            ctaAction = {
                onboardingAnalytics.log(OnboardingAnalytics.OnboardingSteps.CHOOSE_WALLET)
                router.navigateBack()
                router.navigateTo(
                    route = OnboardingRoutes.landing(featureFlags),
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
            closeAction = {
                router.navigateBack()
            },
            tosUrl = abacusStateManager.environment?.links?.tos,
            privacyPolicyUrl = abacusStateManager.environment?.links?.privacy,
            urlAction = { url ->
                router.navigateTo(url)
            },
        )
    }
}
