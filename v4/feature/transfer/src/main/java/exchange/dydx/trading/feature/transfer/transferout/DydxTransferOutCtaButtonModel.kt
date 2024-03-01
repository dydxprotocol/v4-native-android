package exchange.dydx.trading.feature.transfer.transferout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Subaccount
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.dydxstatemanager.dydxTokenInfo
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.dydxstatemanager.nativeTokenKey
import exchange.dydx.dydxstatemanager.usdcTokenInfo
import exchange.dydx.dydxstatemanager.usdcTokenKey
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.common.navigation.TransferRoutes
import exchange.dydx.trading.feature.shared.DydxScreenResult
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import exchange.dydx.trading.feature.transfer.DydxTransferError
import exchange.dydx.trading.feature.transfer.steps.DydxTransferScreenStep
import exchange.dydx.trading.feature.transfer.utils.DydxTransferInstanceStoring
import exchange.dydx.trading.feature.transfer.utils.chainName
import exchange.dydx.trading.feature.transfer.utils.networkName
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.AsyncEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import org.web3j.tuples.generated.Tuple4
import javax.inject.Inject

@HiltViewModel
class DydxTransferOutCtaButtonModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val parser: ParserProtocol,
    private val router: DydxRouter,
    private val screenResultFlow: MutableStateFlow<@JvmSuppressWildcards DydxScreenResult?>,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val errorFlow: MutableStateFlow<@JvmSuppressWildcards DydxTransferError?>,
    private val transferInstanceStore: DydxTransferInstanceStoring,
) : ViewModel(), DydxViewModel {
    private val isSubmittingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val state: Flow<DydxTransferOutCtaButton.ViewState?> =
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
    ): DydxTransferOutCtaButton.ViewState {
        return DydxTransferOutCtaButton.ViewState(
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
                            InputCtaButton.State.Disabled(
                                firstBlockingError.resources.action?.localizedString(localizer),
                            )
                        } else if (transferError != null) {
                            InputCtaButton.State.Disabled(
                                localizer.localize("APP.GENERAL.ERROR"),
                            )
                        } else {
                            InputCtaButton.State.Enabled(
                                localizer.localize("APP.DIRECT_TRANSFER_MODAL.CONFIRM_TRANSFER"),
                            )
                        }
                    }
                    else -> InputCtaButton.State.Disabled(
                        localizer.localize("APP.DIRECT_TRANSFER_MODAL.ENTER_TRANSFER_AMOUNT"),
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
                            transferOut(transferInput)
                        }
                    }
                },
            ),
        )
    }

    private fun hasValidSize(transferInput: TransferInput?): Boolean {
        val size = parser.asDouble(transferInput?.size?.size) ?: 0.0
        val usdcSize = parser.asDouble(transferInput?.size?.usdcSize) ?: 0.0
        return size > 0.0 || usdcSize > 0.0
    }

    private fun transferOut(
        transferInput: TransferInput,
    ) {
        combine(
            abacusStateManager.state.accountBalance(abacusStateManager.environment?.dydxTokenInfo?.denom),
            abacusStateManager.state.accountBalance(abacusStateManager.environment?.usdcTokenInfo?.denom),
            abacusStateManager.state.currentWallet.mapNotNull { it },
            abacusStateManager.state.selectedSubaccount.mapNotNull { it },
        ) { dydxTokenAmount, usdcTokenAmount, wallet, selectedSubaccount ->
            Tuple4(dydxTokenAmount, usdcTokenAmount, wallet, selectedSubaccount)
        }
            .take(1)
            .onEach { (dydxTokenAmount, usdcTokenAmount, wallet, selectedSubaccount) ->
                processTranferOut(
                    transferInput = transferInput,
                    dydxTokenAmount = dydxTokenAmount,
                    usdcTokenAmount = usdcTokenAmount,
                    wallet = wallet,
                    selectedSubaccount = selectedSubaccount,
                )
            }
            .launchIn(viewModelScope)
    }

    private fun processTranferOut(
        transferInput: TransferInput,
        dydxTokenAmount: Double?,
        usdcTokenAmount: Double?,
        wallet: DydxWalletInstance,
        selectedSubaccount: Subaccount,
    ) {
        val destinationAddress = transferInput.address ?: return
        val originationAddress = wallet.cosmoAddress ?: return

        DydxTransferScreenStep(
            originationAddress = originationAddress,
            destinationAddress = destinationAddress,
            transferInput = transferInput,
            abacusStateManager = abacusStateManager,
        ).run()
            .take(1)
            .flatMapLatest { event ->
                val emptyFlow = flowOf<Flow<AsyncEvent<Unit, String>>>()
                val eventResult = event as? AsyncEvent.Result ?: return@flatMapLatest emptyFlow
                val result = eventResult.result
                screenResultFlow.value = result
                when (result) {
                    DydxScreenResult.NoRestriction -> {
                        when (transferInput.token) {
                            abacusStateManager.usdcTokenKey ->
                                DydxTransferOutUSDCStep(
                                    transferInput = transferInput,
                                    selectedSubaccount = selectedSubaccount,
                                    usdcTokenAmount = usdcTokenAmount,
                                    cosmosClient = cosmosClient,
                                    parser = parser,
                                    localizer = localizer,
                                ).run()

                            abacusStateManager.nativeTokenKey ->
                                DydxTransferOutDYDXStep(
                                    transferInput = transferInput,
                                    dydxTokenAmount = dydxTokenAmount,
                                    cosmosClient = cosmosClient,
                                    parser = parser,
                                    localizer = localizer,
                                ).run()

                            else -> {
                                isSubmittingFlow.value = false
                                emptyFlow
                            }
                        }
                    }

                    else -> {
                        isSubmittingFlow.value = false
                        emptyFlow
                    }
                }
            }
            .onEach { event ->
                val event = event as? AsyncEvent.Result<String> ?: return@onEach
                val hash = event.result
                val error = event.error

                if (hash != null) {
                    abacusStateManager.resetTransferInputFields()
                    transferInstanceStore.addTransferHash(
                        hash = hash,
                        fromChainName = abacusStateManager.environment?.chainName,
                        toChainName = transferInput.chainName ?: transferInput.networkName,
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

                isSubmittingFlow.value = false
            }
            .launchIn(viewModelScope)
    }
}
