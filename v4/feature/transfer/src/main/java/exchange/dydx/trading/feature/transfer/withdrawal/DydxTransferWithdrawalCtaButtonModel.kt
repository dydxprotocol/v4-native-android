package exchange.dydx.trading.feature.transfer.withdrawal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.common.navigation.TransferRoutes
import exchange.dydx.trading.feature.shared.DydxScreenResult
import exchange.dydx.trading.feature.shared.analytics.TransferAnalytics
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import exchange.dydx.trading.feature.transfer.DydxTransferError
import exchange.dydx.trading.feature.transfer.steps.DydxTransferScreenStep
import exchange.dydx.trading.feature.transfer.utils.DydxTransferInstanceStoring
import exchange.dydx.trading.feature.transfer.utils.chainName
import exchange.dydx.trading.feature.transfer.utils.networkName
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.runWithLogs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DydxTransferWithdrawalCtaButtonModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val parser: ParserProtocol,
    private val router: DydxRouter,
    private val screenResultFlow: MutableStateFlow<@JvmSuppressWildcards DydxScreenResult?>,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val errorFlow: MutableStateFlow<@JvmSuppressWildcards DydxTransferError?>,
    private val transferInstanceStore: DydxTransferInstanceStoring,
    private val transferAnalytics: TransferAnalytics,
    @CoroutineScopes.App private val appScope: CoroutineScope,
) : ViewModel(), DydxViewModel {
    private val isSubmittingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val state: Flow<DydxTransferWithdrawalCtaButton.ViewState?> =
        combine(
            abacusStateManager.state.transferInput,
            abacusStateManager.state.validationErrors,
            abacusStateManager.state.onboarded,
            isSubmittingFlow,
        ) { transferInput, validationErrors, onboarded, isSubmitting ->
            createViewState(transferInput, validationErrors, onboarded, isSubmitting)
        }
            .distinctUntilChanged()

    private fun createViewState(
        transferInput: TransferInput?,
        tradeErrors: List<ValidationError>,
        isOnboarded: Boolean,
        isSubmitting: Boolean,
    ): DydxTransferWithdrawalCtaButton.ViewState {
        return DydxTransferWithdrawalCtaButton.ViewState(
            ctaButton = InputCtaButton.ViewState(
                localizer = localizer,
                ctaButtonState = when {
                    isSubmitting -> InputCtaButton.State.Disabled(
                        localizer.localize("APP.TRADE.SUBMITTING_ORDER"),
                    )
                    !isOnboarded -> InputCtaButton.State.Disabled(
                        localizer.localize("APP.GENERAL.CONNECT_WALLET"),
                    )
                    hasValidSize(transferInput) -> {
                        val firstBlockingError = tradeErrors.firstOrNull { it.type == ErrorType.required || it.type == ErrorType.error }
                        val transferError = transferInput?.errors
                        if (firstBlockingError != null) {
                            if (transferInput?.requestPayload == null) {
                                InputCtaButton.State.Thinking
                            } else {
                                InputCtaButton.State.Disabled(
                                    firstBlockingError.resources.action?.localizedString(localizer),
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
                                    localizer.localize("APP.GENERAL.CONFIRM_WITHDRAW"),
                                )
                            }
                        }
                    }
                    else -> InputCtaButton.State.Disabled(
                        localizer.localize("APP.WITHDRAW_MODAL.ENTER_WITHDRAW_AMOUNT"),
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
                            withdraw(transferInput)
                        }
                    }
                },
            ),
        )
    }

    private fun hasValidSize(transferInput: TransferInput?): Boolean {
        val usdcSize = parser.asDouble(transferInput?.size?.usdcSize) ?: 0.0
        return usdcSize > 0.0
    }

    private fun withdraw(
        transferInput: TransferInput,
    ) {
        combine(
            abacusStateManager.state.currentWallet.mapNotNull { it },
            abacusStateManager.state.selectedSubaccount.mapNotNull { it },
        ) { wallet, selectedSubaccount ->
            Pair(wallet, selectedSubaccount)
        }
            .take(1)
            .onEach { (wallet, selectedSubaccount) ->
                processWithdraw(transferInput, wallet, selectedSubaccount)
            }
            .launchIn(viewModelScope)
    }

    private fun processWithdraw(
        transferInput: TransferInput,
        wallet: DydxWalletInstance?,
        selectedSubaccount: Subaccount,
    ) {
        val destinationAddress = transferInput.address ?: return
        val originationAddress = wallet?.cosmoAddress ?: return

        appScope.launch {
            val transferScreenResult = DydxTransferScreenStep(
                originationAddress = originationAddress,
                destinationAddress = destinationAddress,
                transferInput = transferInput,
                abacusStateManager = abacusStateManager,
            ).runWithLogs()
            val result = transferScreenResult.getOrNull()
            screenResultFlow.value = result
            val withdrawResult = when (result) {
                DydxScreenResult.NoRestriction -> {
                    DydxWithdrawToIBCStep(
                        transferInput = transferInput,
                        selectedSubaccount = selectedSubaccount,
                        cosmosClient = cosmosClient,
                        parser = parser,
                        localizer = localizer,
                        abacusStateManager = abacusStateManager,
                    ).runWithLogs()
                }

                else -> {
                    isSubmittingFlow.value = false
                    return@launch
                }
            }

            val hash = withdrawResult.getOrNull()?.lowercase()
            if (hash != null) {
                transferAnalytics.logWithdrawal(transferInput)
                transferInstanceStore.addTransferHash(
                    hash = hash,
                    fromChainName = abacusStateManager.environment?.chainName,
                    toChainName = transferInput.chainName ?: transferInput.networkName,
                    transferInput = transferInput,
                )
                abacusStateManager.resetTransferInputFields()
                router.navigateBack()
                router.navigateTo(
                    route = TransferRoutes.transfer_status + "/$hash",
                    presentation = DydxRouter.Presentation.Modal,
                )
            } else {
                errorFlow.value = DydxTransferError(
                    message = withdrawResult.exceptionOrNull()?.localizedMessage ?: "",
                )
            }

            isSubmittingFlow.value = false
        }
    }
}
