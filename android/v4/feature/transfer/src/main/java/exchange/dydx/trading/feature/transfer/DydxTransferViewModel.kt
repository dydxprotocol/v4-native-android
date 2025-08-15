package exchange.dydx.trading.feature.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.TransferInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.PlatformInfoViewModel
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.receipt.ReceiptType
import exchange.dydx.trading.feature.shared.DydxScreenResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class DydxTransferViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val selectionFlow: Flow<@JvmSuppressWildcards DydxTransferSectionsView.Selection>,
    val receiptTypeFlow: MutableStateFlow<@JvmSuppressWildcards ReceiptType?>,
    val toaster: PlatformInfo,
    private val errorFlow: MutableStateFlow<@JvmSuppressWildcards DydxTransferError?>,
    private val screenResultFlow: MutableStateFlow<@JvmSuppressWildcards DydxScreenResult?>,
    private val featureFlags: DydxFeatureFlags,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTransferView.ViewState?> = selectionFlow
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    init {
        receiptTypeFlow.value = ReceiptType.Transfer

        errorFlow
            .onEach { error ->
                if (error != null) {
                    toaster.show(
                        title = error.title ?: localizer.localize("APP.GENERAL.ERROR"),
                        message = error.message ?: "",
                        buttonTitle = localizer.localize("APP.GENERAL.OK"),
                        type = PlatformInfoViewModel.Type.Error,
                        buttonAction = {
                            errorFlow.value = null
                        },
                    )
                }
            }
            .launchIn(viewModelScope)

        screenResultFlow
            .onEach { screenResult ->
                screenResult?.showRestrictionAlert(
                    toaster = toaster,
                    localizer = localizer,
                    abacusStateManager = abacusStateManager,
                    buttonAction = {
                        screenResultFlow.value = null
                    },
                )
            }
            .launchIn(viewModelScope)

        combine(
            abacusStateManager.state.transferInput,
            selectionFlow,
        ) { transferInput, selection ->
            when (selection) {
                DydxTransferSectionsView.Selection.Deposit -> {
                    if (transferInput?.type != TransferType.deposit) {
                        abacusStateManager.startDeposit()
                    }
                }
                DydxTransferSectionsView.Selection.Withdrawal -> {
                    if (transferInput?.type != TransferType.withdrawal) {
                        abacusStateManager.startWithdrawal()
                        abacusStateManager.state.currentWallet
                            .take(1)
                            .onStart { delay(100) }
                            .onEach { wallet ->
                                abacusStateManager.transfer(
                                    input = wallet?.ethereumAddress,
                                    type = TransferInputField.address,
                                )
                            }
                            .launchIn(viewModelScope)
                    }
                }
                DydxTransferSectionsView.Selection.TransferOut -> {
                    if (transferInput?.type != TransferType.transferOut) {
                        abacusStateManager.transfer(input = null, type = TransferInputField.address)
                        abacusStateManager.startTransferOut()
                    }
                }
                DydxTransferSectionsView.Selection.Faucet -> {
                }
            }
        }
            .launchIn(viewModelScope)
    }

    private fun createViewState(
        selection: DydxTransferSectionsView.Selection
    ): DydxTransferView.ViewState {
        return DydxTransferView.ViewState(
            localizer = localizer,
            selection = selection,
            closeAction = {
                router.navigateBack()
            },
        )
    }
}
