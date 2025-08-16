package exchange.dydx.trading.feature.transfer.deposit

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import exchange.dydx.abacus.functional.ClientTrackableEventType
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxCartera.CarteraProvider
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.trading.common.BuildConfig
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.featureflags.DydxDoubleFeatureFlag
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.common.navigation.TransferRoutes
import exchange.dydx.trading.feature.receipt.streams.TransferRouteSelection
import exchange.dydx.trading.feature.receipt.streams.TransferRouteSelectionInfo
import exchange.dydx.trading.feature.shared.TransferTokenDetails
import exchange.dydx.trading.feature.shared.analytics.OnboardingAnalytics
import exchange.dydx.trading.feature.shared.analytics.TransferAnalytics
import exchange.dydx.trading.feature.shared.analytics.logSharedEvent
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import exchange.dydx.trading.feature.transfer.DydxTransferError
import exchange.dydx.trading.feature.transfer.deposit.steps.DydxTransferDepositStep
import exchange.dydx.trading.feature.transfer.tokenAddress
import exchange.dydx.trading.feature.transfer.utils.DydxTransferInstanceStoring
import exchange.dydx.trading.feature.transfer.utils.chainName
import exchange.dydx.trading.feature.transfer.utils.networkName
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import exchange.dydx.utilities.utils.runWithLogs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DydxTransferDepositCtaButtonModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val parser: ParserProtocol,
    private val router: DydxRouter,
    @ApplicationContext private val context: Context,
    private val transferInstanceStore: DydxTransferInstanceStoring,
    private val errorFlow: MutableStateFlow<@JvmSuppressWildcards DydxTransferError?>,
    private val onboardingAnalytics: OnboardingAnalytics,
    private val transferAnalytics: TransferAnalytics,
    @CoroutineScopes.App private val appScope: CoroutineScope,
    private val featureFlags: DydxFeatureFlags,
    private val formatter: DydxFormatter,
    private val transferRouteSelectionInfo: TransferRouteSelectionInfo,
    private val transferTokenDetails: TransferTokenDetails,
    private val tracker: Tracking,
) : ViewModel(), DydxViewModel {
    private val carteraProvider: CarteraProvider = CarteraProvider(context)
    private val isSubmittingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val state: Flow<DydxTransferDepositCtaButton.ViewState?> =
        combine(
            abacusStateManager.state.transferInput,
            abacusStateManager.state.validationErrors,
            abacusStateManager.state.onboarded,
            isSubmittingFlow,
            abacusStateManager.state.currentWallet,
            transferRouteSelectionInfo.selected,
        ) { array ->
            createViewState(
                array[0] as TransferInput?,
                array[1] as List<ValidationError>,
                array[2] as Boolean,
                array[3] as Boolean,
                array[4] as DydxWalletInstance?,
                array[5] as TransferRouteSelection,
            )
        }
            .distinctUntilChanged()

    private fun createViewState(
        transferInput: TransferInput?,
        tradeErrors: List<ValidationError>,
        isOnboarded: Boolean,
        isSubmitting: Boolean,
        wallet: DydxWalletInstance?,
        selectedRoute: TransferRouteSelection,
    ): DydxTransferDepositCtaButton.ViewState {
        return DydxTransferDepositCtaButton.ViewState(
            ctaButton = InputCtaButton.ViewState(
                localizer = localizer,
                ctaButtonState = when {
                    isSubmitting -> InputCtaButton.State.Disabled(
                        localizer.localize("APP.TRADE.SUBMITTING_ORDER"),
                    )
                    !isOnboarded -> InputCtaButton.State.Disabled(
                        localizer.localize("APP.TURNKEY_ONBOARD.SIGN_IN_TITLE"),
                    )
                    hasValidSize(transferInput) -> {
                        if (belowMinSizeForDeposit(transferInput)) {
                            val minUsdcAmount = if (BuildConfig.DEBUG) {
                                1.0
                            } else {
                                featureFlags.doubleForFeature(DydxDoubleFeatureFlag.min_usdc_for_deposit)
                            }
                            val minAmountString = formatter.dollar(minUsdcAmount, digits = 2)
                            InputCtaButton.State.Disabled(
                                localizer.localizeWithParams(
                                    "APP.ONBOARDING.MINIMUM_DEPOSIT",
                                    params = mapOf(
                                        "MIN_DEPOSIT_USDC" to (minAmountString ?: ""),
                                    ),
                                ),
                            )
                        } else {
                            val firstBlockingError =
                                tradeErrors.firstOrNull { it.type == ErrorType.required || it.type == ErrorType.error }
                            val transferError = transferInput?.errors
                            if (firstBlockingError != null) {
                                if (transferInput?.requestPayload == null) {
                                    InputCtaButton.State.Thinking
                                } else {
                                    InputCtaButton.State.Disabled(
                                        firstBlockingError.resources.action?.localizedString(
                                            localizer,
                                        ),
                                    )
                                }
                            } else if (transferError != null) {
                                InputCtaButton.State.Disabled(
                                    localizer.localize("APP.GENERAL.ERROR"),
                                )
                            } else {
                                if (transferInput?.requestPayload == null) {
                                    InputCtaButton.State.Thinking
                                } else {
                                    InputCtaButton.State.Enabled(
                                        localizer.localize("APP.GENERAL.CONFIRM_DEPOSIT"),
                                    )
                                }
                            }
                        }
                    }
                    else -> InputCtaButton.State.Disabled(
                        localizer.localize("APP.DEPOSIT_MODAL.ENTER_DEPOSIT_AMOUNT"),
                    )
                },
                ctaAction = {
                    if (!isOnboarded) {
                        router.navigateTo(
                            route = OnboardingRoutes.welcome,
                            presentation = DydxRouter.Presentation.Modal,
                        )
                    } else {
                        isSubmittingFlow.value = true
                        if (transferInput != null) {
                            deposit(transferInput, wallet, selectedRoute)
                        }
                    }
                },
            ),
        )
    }

    private fun hasValidSize(transferInput: TransferInput?): Boolean {
        val size = parser.asDouble(transferInput?.size?.size) ?: 0.0
        return size > 0.0
    }

    private fun belowMinSizeForDeposit(transferInput: TransferInput?): Boolean {
        val size = parser.asDouble(transferInput?.size?.usdcSize) ?: 0.0
        val minSize = if (BuildConfig.DEBUG) {
            1.0
        } else {
            featureFlags.doubleForFeature(DydxDoubleFeatureFlag.min_usdc_for_deposit)
        }
        return size < minSize * .99 // since USDC price is not always == $1.00
    }

    private fun deposit(
        transferInput: TransferInput,
        wallet: DydxWalletInstance?,
        selectedRoute: TransferRouteSelection,
    ) {
        val wallet = wallet ?: return
        val walletAddress = wallet.ethereumAddress ?: return
        val tokenAddress = transferInput.tokenAddress(featureFlags) ?: return

        val chain = transferInput.chain ?: return
        val chainRpc = transferInput.resources?.chainResources?.get(chain)?.rpc

        onboardingAnalytics.log(OnboardingAnalytics.OnboardingSteps.DEPOSIT_INITIATED)
        val summary = when (selectedRoute) {
            TransferRouteSelection.Instant -> {
                transferInput.goFastSummary
            }
            TransferRouteSelection.Regular -> {
                transferInput.summary
            }
        }
        tracker.logSharedEvent(
            ClientTrackableEventType.DepositInitiatedEvent(
                transferInput = transferInput,
                summary = summary,
                isInstantDeposit = selectedRoute == TransferRouteSelection.Instant,
            ),
        )

        appScope.launch {
            val event =
                DydxTransferDepositStep(
                    transferInput = transferInput,
                    provider = carteraProvider,
                    walletAddress = walletAddress,
                    walletId = wallet.walletId,
                    chainRpc = chainRpc,
                    tokenAddress = tokenAddress,
                    context = context,
                    selectedRoute = selectedRoute,
                    transferTokenDetails = transferTokenDetails,
                ).runWithLogs()

            isSubmittingFlow.value = false
            val hash = event.getOrNull()
            if (hash != null) {
                onboardingAnalytics.log(OnboardingAnalytics.OnboardingSteps.DEPOSIT_FUNDS)
                tracker.logSharedEvent(
                    ClientTrackableEventType.DepositSubmittedEvent(
                        transferInput = transferInput,
                        summary = summary,
                        txHash = hash,
                        isInstantDeposit = selectedRoute == TransferRouteSelection.Instant,
                    ),
                )

                transferAnalytics.logDeposit(transferInput)
                if (selectedRoute == TransferRouteSelection.Regular) {
                    abacusStateManager.resetTransferInputFields()
                }
                transferInstanceStore.addTransferHash(
                    hash = hash,
                    fromChainName = transferInput.chainName ?: transferInput.networkName,
                    toChainName = abacusStateManager.environment?.chainName,
                    transferInput = transferInput,
                )
                router.navigateBack()
                val transferStatusRoute = when (selectedRoute) {
                    TransferRouteSelection.Instant -> {
                        TransferRoutes.transfer_status_instant
                    }
                    TransferRouteSelection.Regular -> {
                        TransferRoutes.transfer_status
                    }
                }
                router.navigateTo(
                    route = "$transferStatusRoute/$hash",
                    presentation = DydxRouter.Presentation.Modal,
                )
            } else {
                tracker.logSharedEvent(
                    ClientTrackableEventType.DepositErrorEvent(
                        transferInput = transferInput,
                        errorMessage = event.exceptionOrNull()?.message ?: "Deposit error",
                    ),
                )
                errorFlow.value = DydxTransferError(
                    message = event.exceptionOrNull()?.message ?: "Deposit error",
                )
            }
        }
    }
}
