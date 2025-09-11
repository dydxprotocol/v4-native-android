package exchange.dydx.feature.onboarding.turnkey

import android.R.attr.path
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.platformui.designSystem.theme.ThemeSettings
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.R
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.feature.shared.analytics.OnboardingAnalytics
import exchange.dydx.trading.feature.shared.analytics.WalletAnalytics
import exchange.dydx.trading.integration.cosmos.CosmosV4ClientProtocol
import exchange.dydx.trading.integration.react.LocalizerEntry
import exchange.dydx.trading.integration.react.TurnkeyBridgeManagerDelegate
import exchange.dydx.trading.integration.react.TurnkeyReactBridge
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import kotlin.String

private const val TAG = "DydxTurnkeyAuthViewModel"

@HiltViewModel
class DydxTurnkeyAuthViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    @ApplicationContext private val appContext: android.content.Context,
    private val router: DydxRouter,
    private val turnkeyReactBridge: TurnkeyReactBridge,
    private val cosmosV4Client: CosmosV4ClientProtocol,
    private val parser: ParserProtocol,
    private val onboardingAnalytics: OnboardingAnalytics,
    private val walletAnalytics: WalletAnalytics,
    private val logger: Logging,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), DydxViewModel, TurnkeyBridgeManagerDelegate {

    private val token: String? = savedStateHandle["token"]

    init {
        // Need to wait for the bridge to be initialized before setting the delegate
        turnkeyReactBridge.isInitialized
            .filter { it }
            .take(1)
            .onEach {
                turnkeyReactBridge.setBridgeDelegate(this)
                if (token != null) {
                    Thread.sleep(1000)
                    turnkeyReactBridge.emailTokenReceived(token = token)
                }
            }
            .launchIn(viewModelScope)
    }

    val state: Flow<DydxTurnkeyAuthView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxTurnkeyAuthView.ViewState? {
        val indexerUrl = abacusStateManager.environment?.endpoints?.indexers?.firstOrNull()?.api ?: return null
        val tosUrl = abacusStateManager.environment?.links?.tos ?: return null
        val privacyUrl = abacusStateManager.environment?.links?.privacy ?: return null

        val initialProperties: Map<String, String> = mapOf(
            // From https://console.cloud.google.com/auth/clients?inv=1&invt=Ab1olg&project=dydx-v4
            "googleClientId" to appContext.getString(R.string.google_client_id),
            "appScheme" to appContext.getString(R.string.app_scheme),
            "turnkeyUrl" to "https://api.turnkey.com",
            // From Turnkey console
            "turnkeyOrgId" to appContext.getString(R.string.turnkey_org_id),
            "backendApiUrl" to indexerUrl,
            "deploymentUri" to abacusStateManager.deploymentUri + "/",
            "theme" to (ThemeSettings.shared.themeConfig.value?.id ?: "dark"),
        )

        // The terms string contains HTML links, so we need to construct it here
        val tos = "<a href=\"${tosUrl}\">${localizer.localize(path = "APP.HEADER.TERMS_OF_USE")}</a>"
        val privacy = "<a href=\"${privacyUrl}\">${localizer.localize(path = "APP.ONBOARDING.PRIVACY_POLICY")}</a>"
        val terms = localizer.localizeWithParams(
            path = "APP.ONBOARDING.TOS_SHORT",
            params = mapOf(
                "TERMS_LINK" to tos,
                "PRIVACY_POLICY_LINK" to privacy,
            ),
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
            LocalizerEntry(path = "APP.TURNKEY_ONBOARD.SIGN_IN_GOOGLE"),
            LocalizerEntry(path = "APP.TURNKEY_ONBOARD.SIGN_IN_APPLE"),
            LocalizerEntry(path = "APP.TURNKEY_ONBOARD.SIGN_IN_EMAIL"),
            LocalizerEntry(path = "APP.TURNKEY_ONBOARD.CONTINUE_SIGN_IN_DESCRIPTION"),
            LocalizerEntry(path = "APP.GENERAL.OR"),
            LocalizerEntry(path = "APP.ONBOARDING.TOS_SHORT", localized = terms),
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
                route = OnboardingRoutes.wallet_list + "?backButtonRoute=${OnboardingRoutes.turnkey}",
                presentation = DydxRouter.Presentation.Modal,
            )
        }
    }

    override fun onAuthRouteToDesktopQR() {
        viewModelScope.launch {
            router.navigateBack()
            router.navigateTo(
                route = OnboardingRoutes.desktop_scan + "?backButtonRoute=${OnboardingRoutes.turnkey}",
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
        cosmosV4Client.deriveCosmosKey(signature = onboardingSignature) { data ->
            if (data == null) {
                return@deriveCosmosKey
            }

            val json = Json.parseToJsonElement(data)
            val map = json.jsonObject.toMap()
            val dydxMnemonic = parser.asString(map["mnemonic"])
            val cosmosAddress = parser.asString(map["address"])

            if (dydxMnemonic.isNullOrEmpty() || cosmosAddress.isNullOrEmpty()) {
                logger.e(TAG, "Failed to derive Cosmos key from Turnkey")
                return@deriveCosmosKey
            }

            turnkeyReactBridge.uploadDydxAddress(dydxAddress = cosmosAddress) { result ->
                if (result != "success") {
                    // Log error but continue
                    logger.e(TAG, "Failed to upload dYdX address to Turnkey: $result")
                    return@uploadDydxAddress
                }

                onboardingAnalytics.log(OnboardingAnalytics.OnboardingSteps.KEY_DERIVATION)
                walletAnalytics.logConnected(walletId = "turnkey")

                abacusStateManager.setV4(
                    ethereumAddress = evmAddress,
                    walletId = "turnkey",
                    cosmosAddress = cosmosAddress,
                    dydxMnemonic = dydxMnemonic,
                    isNew = true,
                    svmAddress = svmAddress,
                    avalancheAddress = null,
                    sourceWalletMnemonic = mnemonics,
                    loginMethod = loginMethod,
                    userEmail = userEmail,
                )

                onboardingAnalytics.log(OnboardingAnalytics.OnboardingSteps.ACKNOWLEDGE_TERMS)

                viewModelScope.launch {
                    router.navigateBack()
                    router.navigateToRoot(excludeRoot = false)
                    router.navigateTo(
                        route = OnboardingRoutes.deposit_prompt,
                        presentation = DydxRouter.Presentation.Modal,
                    )
                }
            }
        }
    }

    override fun onAppleAuthRequest(nonce: String) {
        // No op
    }
}
