package exchange.dydx.trading.feature.transfer.deposit

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.cartera.CarteraProvider
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.common.navigation.TransferRoutes
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import exchange.dydx.trading.feature.transfer.DydxTransferError
import exchange.dydx.trading.feature.transfer.utils.DydxTransferInstanceStoring
import exchange.dydx.trading.feature.transfer.utils.chainName
import exchange.dydx.trading.feature.transfer.utils.networkName
import exchange.dydx.utilities.utils.AsyncEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
        ) { transferInput, validationErrors, onboarded, isSubmitting, wallet ->
            createViewState(transferInput, validationErrors, onboarded, isSubmitting, wallet)
        }
            .distinctUntilChanged()

    private fun createViewState(
        transferInput: TransferInput?,
        tradeErrors: List<ValidationError>,
        isOnboarded: Boolean,
        isSubmitting: Boolean,
        wallet: DydxWalletInstance?,
    ): DydxTransferDepositCtaButton.ViewState {
        return DydxTransferDepositCtaButton.ViewState(
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
                                    localizer.localize("APP.GENERAL.CONFIRM_DEPOSIT"),
                                )
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
                            deposit(transferInput, wallet)
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

    private fun deposit(
        transferInput: TransferInput,
        wallet: DydxWalletInstance?,
    ) {
        val wallet = wallet ?: return
        val chain = transferInput.chain ?: return
        val token = transferInput.token ?: return
        val chainRpc = transferInput.resources?.chainResources?.get(chain)?.rpc ?: return
        val tokenAddress = transferInput.resources?.tokenResources?.get(token)?.address ?: return

        DydxTransferDepositStep(
            transferInput = transferInput,
            provider = carteraProvider,
            walletAddress = wallet.ethereumAddress,
            walletId = wallet.walletId,
            chainRpc = chainRpc,
            tokenAddress = tokenAddress,
            context = context,
        )
            .run()
            .onEach { event ->
                val eventResult = event as? AsyncEvent.Result ?: return@onEach
                isSubmittingFlow.value = false
                val hash = eventResult.result
                val error = eventResult.error
                if (hash != null) {
                    abacusStateManager.resetTransferInputFields()
                    transferInstanceStore.addTransferHash(
                        hash = hash,
                        fromChainName = transferInput.chainName ?: transferInput.networkName,
                        toChainName = abacusStateManager.environment?.chainName,
                        transferInput = transferInput,
                    )
                    router.navigateBack()
                    router.navigateTo(
                        route = TransferRoutes.transfer_status + "/$hash",
                        presentation = DydxRouter.Presentation.Modal,
                    )
                } else if (error != null) {
                    errorFlow.value = DydxTransferError(
                        message = error.localizedMessage ?: "",
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
