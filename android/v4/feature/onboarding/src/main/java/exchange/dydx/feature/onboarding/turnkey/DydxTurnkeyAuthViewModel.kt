package exchange.dydx.feature.onboarding.turnkey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.R
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.integration.react.LocalizerEntry
import exchange.dydx.trading.integration.react.TurnkeyBridgeManagerDelegate
import exchange.dydx.trading.integration.react.TurnkeyReactBridge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DydxTurnkeyAuthViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    @ApplicationContext private val appContext: android.content.Context,
    private val router: DydxRouter,
    private val turnkeyReactBridge: TurnkeyReactBridge,
) : ViewModel(), DydxViewModel, TurnkeyBridgeManagerDelegate {

    init {
        turnkeyReactBridge.setBridgeDelegate(this)
    }

    override fun onCleared() {
        super.onCleared()
        turnkeyReactBridge.setBridgeDelegate(null)
    }

    val state: Flow<DydxTurnkeyAuthView.ViewState?> = abacusStateManager.state.marketSummary
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(marketSummary: PerpetualMarketSummary?): DydxTurnkeyAuthView.ViewState {
        val initialProperties: Map<String, String> = mapOf(
            // From https://console.cloud.google.com/auth/clients?inv=1&invt=Ab1olg&project=dydx-v4
            "googleClientId" to "441463123744-a02e7s84okic2ggqgdo7e7hlgpvkj3p8.apps.googleusercontent.com",
            "appScheme" to appContext.getString(R.string.app_scheme),
            "turnkeyUrl" to "https://api.turnkey.com",
            // From Turnkey console
            "turnkeyOrgId" to "3174ac51-1637-47d8-9456-19549963e2ed",
            // Indexer backend
            "backendApiUrl" to "http://dev2-indexer-apne1-lb-public-2076363889.ap-northeast-1.elb.amazonaws.com",
            "theme" to "dark",
        )

        val localizerEntries = listOf(
            LocalizerEntry(path = "APP.TURNKEY_ONBOARD.SIGN_IN_TITLE"),
            LocalizerEntry(path = "APP.TURNKEY_ONBOARD.SIGN_IN_DESCRIPTION"),
            LocalizerEntry(path = "APP.TURNKEY_ONBOARD.SIGN_IN_PASSKEY"),
            LocalizerEntry(path = "APP.TURNKEY_ONBOARD.SIGN_IN_WALLET"),
            LocalizerEntry(path = "APP.TURNKEY_ONBOARD.SIGN_IN_DESKTOP"),
            LocalizerEntry(path = "APP.TURNKEY_ONBOARD.SUBMIT"),
            LocalizerEntry(path = "APP.TURNKEY_ONBOARD.EMAIL_PLACEHOLDER"),
            LocalizerEntry(path = "APP.TURNKEY_ONBOARD.CHECK_EMAIL_TITLE"),
            LocalizerEntry(path = "APP.TURNKEY_ONBOARD.CHECK_EMAIL_DESCRIPTION"),
            LocalizerEntry(path = "APP.TURNKEY_ONBOARD.RESEND"),
            LocalizerEntry(path = "APP.GENERAL.OR"),
        )

        return DydxTurnkeyAuthView.ViewState(
            localizer = localizer,
            initialProperties = initialProperties,
            localizerEntries = localizerEntries,
            closeAction = {
                router.navigateBack()
            },
        )
    }

    override fun onAuthRouteToWallet() {
        viewModelScope.launch {
            router.navigateBack()
            router.navigateTo(
                route = OnboardingRoutes.wallet_list,
                presentation = DydxRouter.Presentation.Modal,
            )
        }
    }

    override fun onAuthRouteToDesktopQR() {
        viewModelScope.launch {
            router.navigateBack()
            router.navigateTo(
                route = OnboardingRoutes.desktop_scan,
                presentation = DydxRouter.Presentation.Modal,
            )
        }
    }

    override fun onAuthCompleted(
        onboardingSignature: String,
        evmAddress: String,
        svmAddress: String,
        mnemonics: String,
        loginMethod: String,
        userEmail: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun onAppleAuthRequest(nonce: String) {
        // No op
    }
}
