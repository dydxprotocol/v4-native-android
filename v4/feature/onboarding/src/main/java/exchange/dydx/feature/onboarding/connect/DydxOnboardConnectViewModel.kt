package exchange.dydx.feature.onboarding.connect

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.cartera.CarteraConfig
import exchange.dydx.dydxCartera.DydxWalletSetup
import exchange.dydx.dydxCartera.imageUrl
import exchange.dydx.dydxCartera.v4.DydxV4WalletSetup
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.Toast
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.feature.shared.analytics.OnboardingAnalytics
import exchange.dydx.trading.feature.shared.analytics.WalletAnalytics
import exchange.dydx.trading.feature.shared.views.ProgressStepView
import exchange.dydx.trading.integration.cosmos.CosmosV4ClientProtocol
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

private const val TAG = "DydxOnboardConnectViewModel"

@HiltViewModel
class DydxOnboardConnectViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
    private val cosmosV4Client: CosmosV4ClientProtocol,
    private val parser: ParserProtocol,
    val abacusStateManager: AbacusStateManagerProtocol,
    val toaster: PlatformInfo,
    private val mutableSetupStatusFlow: MutableStateFlow<DydxWalletSetup.Status.Signed?>,
    private val onboardingAnalytics: OnboardingAnalytics,
    private val walletAnalytics: WalletAnalytics,
    private val logger: Logging,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), DydxViewModel {

    private val walletId: String = checkNotNull(savedStateHandle["walletId"])

    private var walletSetup: DydxWalletSetup? = null

    private val _state = MutableStateFlow(createViewState())
    val state: Flow<DydxOnboardConnectView.ViewState> = _state

    init {
        val walletSetup = DydxV4WalletSetup(context, cosmosV4Client, parser, logger)

//        try {
//            walletSetup.tryConnet()
//        } catch (e: Exception) {
//            logger.e(TAG, "Failed to connect wallet")
//        }

        walletSetup.status.onEach { walletStatus ->
            when (walletStatus) {
                is DydxWalletSetup.Status.Started -> {
                    _state.update { state ->
                        state.copy(
                            steps = listOf(
                                step1(status = ProgressStepView.Status.InProgress),
                                step2(),
                            ),
                            linkWalletButtonEnabled = false,
                        )
                    }
                }

                is DydxWalletSetup.Status.Connected -> {
                    _state.update { state ->
                        state.copy(
                            steps = listOf(
                                step1(status = ProgressStepView.Status.Completed),
                                step2(status = ProgressStepView.Status.InProgress),
                            ),
                            linkWalletButtonEnabled = false,
                        )
                    }
                }

                is DydxWalletSetup.Status.Signed -> {
                    onboardingAnalytics.log(OnboardingAnalytics.OnboardingSteps.KEY_DERIVATION)
                    walletAnalytics.logConnected(walletId)

                    _state.update { state ->
                        state.copy(
                            steps = listOf(
                                step1(status = ProgressStepView.Status.Completed),
                                step2(status = ProgressStepView.Status.Completed),
                            ),
                            linkWalletButtonEnabled = false,
                        )
                    }

                    mutableSetupStatusFlow.value = walletStatus

                    router.navigateBack()
                    router.navigateTo(
                        route = OnboardingRoutes.tos,
                        presentation = DydxRouter.Presentation.Modal,
                    )

                    walletSetup.stop()
                }

                is DydxWalletSetup.Status.Error -> {
                    val error = walletStatus.error
                    val message = error.message ?: localizer.localize("APP.GENERAL.ERROR")
                    toaster.show(
                        message = message,
                        type = Toast.Type.Error,
                    )
                }

                else -> {}
            }
        }.launchIn(viewModelScope)

        this.walletSetup = walletSetup
    }

    private fun createViewState(): DydxOnboardConnectView.ViewState {
        val wallet = CarteraConfig.shared?.wallets?.firstOrNull { it.id == walletId }
        if (wallet == null) {
            logger.e(TAG, "Wallet not found: $walletId")
        }
        val folder = abacusStateManager.environment?.walletConnection?.images
        val walletIcon = wallet?.imageUrl(folder)

        return DydxOnboardConnectView.ViewState(
            localizer = localizer,
            steps = listOf(step1(), step2()),
            closeButtonHandler = {
                walletSetup?.stop()
                router.navigateBack()
            },
            linkWalletAction = {
                val ethereumChainId = parser.asInt(abacusStateManager.environment?.ethereumChainId)
                val signTypedDataAction =
                    abacusStateManager.environment?.walletConnection?.signTypedDataAction
                val signTypedDataDomainName =
                    abacusStateManager.environment?.walletConnection?.signTypedDataDomainName
                if (ethereumChainId != null && signTypedDataAction != null && signTypedDataDomainName != null) {
                    walletSetup?.start(
                        walletId = walletId,
                        ethereumChainId = ethereumChainId,
                        signTypedDataAction = signTypedDataAction,
                        signTypedDataDomainName = signTypedDataDomainName,
                    )
                }
            },
            walletIcon = walletIcon,
        )
    }

    private fun step1(status: ProgressStepView.Status = ProgressStepView.Status.Custom("1")): ProgressStepView.ViewState {
        return ProgressStepView.ViewState(
            title = localizer.localize("APP.ONBOARDING.CONNECT_YOUR_WALLET"),
            subtitle = localizer.localize("APP.GENERAL.CONNECT_WALLET_TEXT"),
            status = status,
        )
    }

    private fun step2(status: ProgressStepView.Status = ProgressStepView.Status.Custom("2")): ProgressStepView.ViewState {
        return ProgressStepView.ViewState(
            title = localizer.localize("APP.ONBOARDING.VERIFY_OWNERSHIP"),
            subtitle = localizer.localize("APP.ONBOARDING.CONFIRM_OWNERSHIP"),
            status = status,
        )
    }
}
