package exchange.dydx.feature.onboarding.tos

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxCartera.DydxWalletSetup
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.analytics.OnboardingAnalytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTosViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val setupStatusFlow: StateFlow<DydxWalletSetup.Status.Signed?>,
    private val onboardingAnalytics: OnboardingAnalytics,
    private val featureFlags: DydxFeatureFlags,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTosView.ViewState?> =
        setupStatusFlow
            .map { status ->
                createViewState(status)
            }
            .distinctUntilChanged()

    private fun createViewState(
        setupStatus: DydxWalletSetup.Status.Signed?
    ): DydxTosView.ViewState {
        return DydxTosView.ViewState(
            localizer = localizer,
            tosUrl = abacusStateManager.environment?.links?.tos,
            privacyPolicyUrl = abacusStateManager.environment?.links?.privacy,
            closeAction = {
                router.navigateBack()
            },
            ctaAction = {
                setupStatus?.let {
                    val result = it.setupResult
                    val ethereumAddress = result.ethereumAddress
                    val cosmosAddress = result.cosmosAddress
                    val mnemonic = result.dydxMnemonic
                    if (cosmosAddress != null && mnemonic != null) {
                        abacusStateManager.setV4(
                            ethereumAddress = ethereumAddress,
                            walletId = result.walletId,
                            cosmosAddress = cosmosAddress,
                            dydxMnemonic = mnemonic,
                            isNew = true,
                            svmAddress = result.svmAddress,
                            avalancheAddress = result.avalancheAddress,
                            sourceWalletMnemonic = result.sourceWalletMnemonic,
                            loginMethod = result.loginMethod,
                            userEmail = result.userEmail,
                        )
                    }

                    onboardingAnalytics.log(OnboardingAnalytics.OnboardingSteps.ACKNOWLEDGE_TERMS)
                }

                router.navigateToRoot(excludeRoot = false)
            },
            urlAction = { url ->
                router.navigateTo(url)
            },
        )
    }
}
