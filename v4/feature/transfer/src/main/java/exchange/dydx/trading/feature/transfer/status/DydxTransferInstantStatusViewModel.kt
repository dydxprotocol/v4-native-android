package exchange.dydx.trading.feature.transfer.status

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.functional.ClientTrackableEventType
import exchange.dydx.abacus.output.TransferStatus
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.transfers.DydxTransferInstance
import exchange.dydx.dydxstatemanager.clientState.transfers.DydxTransferStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.TransferTokenDetails
import exchange.dydx.trading.feature.shared.TransferTokenInfo
import exchange.dydx.trading.feature.shared.analytics.logSharedEvent
import exchange.dydx.trading.feature.transfer.status.DydxTransferInstantStatusView.StatusIcon
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@HiltViewModel
class DydxTransferInstantStatusViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val localizer: LocalizerProtocol,
    val toaster: PlatformInfo,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val transferStateManager: DydxTransferStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    private val transferTokenDetails: TransferTokenDetails,
    private val tracker: Tracking,
) : ViewModel(), DydxViewModel {
    private val transactionHash: String? = savedStateHandle["hash"]
    private val transfer: DydxTransferInstance?
    private var timer: Timer? = null

    init {
        if (transactionHash == null) {
            router.navigateBack()
            transfer = null
        } else {
            val transferState = transferStateManager.state.value
            transfer = transferState?.transfers?.firstOrNull {
                it.transactionHash == transactionHash
            }
            if (transfer != null) {
                fetchTransferStatuses(transfer)
            } else {
                router.navigateBack()
            }
        }
    }

    val state: Flow<DydxTransferInstantStatusView.ViewState?> =
        combine(
            abacusStateManager.state.transferStatuses,
            abacusStateManager.state.transferInput,
            transferTokenDetails.infos,
        ) { statuses, transferInput, tokenInfos ->
            when (transfer?.transferType) {
                DydxTransferInstance.TransferType.DEPOSIT -> createViewState(
                    statuses = statuses,
                    input = transferInput,
                    tokenInfos = tokenInfos,
                )

                else -> null
            }
        }
            .distinctUntilChanged()

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()

        abacusStateManager.resetTransferInputFields()
        transferTokenDetails.refresh()
    }

    private fun fetchTransferStatuses(transfer: DydxTransferInstance) {
        timer?.cancel()
        timer = fixedRateTimer(initialDelay = 0, period = 5_000) {
            abacusStateManager.transferStatus(
                hash = transfer.transactionHash,
                fromChainId = transfer.fromChainId,
                toChainId = transfer.toChainId,
                isCctp = transfer.isCctp ?: false,
                requestId = transfer.requestId,
            )
        }
    }

    private fun createViewState(
        statuses: Map<String, TransferStatus>,
        input: TransferInput?,
        tokenInfos: List<TransferTokenInfo>,
    ): DydxTransferInstantStatusView.ViewState {
        val tokenInfo = tokenInfos.firstOrNull {
            it.tokenAddress == input?.token && it.chainId == input.chain
        }
        val amount = input?.size?.size?.toDoubleOrNull() ?: 0.0
        val status = statuses[transactionHash]
        val completed = if (status != null && tokenInfo != null) {
            routeCompleted(
                transferStatus = status,
                chainId = tokenInfo.chainId,
            )
        } else {
            false
        }
        if (completed && transfer != null && status != null) {
            when (transfer.transferType) {
                DydxTransferInstance.TransferType.DEPOSIT ->
                    tracker.logSharedEvent(
                        ClientTrackableEventType.DepositFinalizedEvent(
                            status = status,
                        ),
                    )
                else -> {}
            }
            stopTrackingTransaction()
        }

        status?.error?.let {
            toaster.show(
                title = localizer.localize("ERRORS.API_STATUS.UNKNOWN_API_ERROR"),
                message = it,
            )
        }

        val usdcSize = input?.size?.usdcSize?.toDoubleOrNull() ?: 0.0

        return DydxTransferInstantStatusView.ViewState(
            localizer = localizer,
            label = localizer.localize("APP.ONBOARDING.YOUR_DEPOSIT"),
            token = tokenInfo?.token?.name,
            tokenIconUri = tokenInfo?.tokenLogoUrl(abacusStateManager.deploymentUri),
            chainIconUri = tokenInfo?.chainLogUrl(abacusStateManager.deploymentUri),
            amount = formatter.raw(number = amount, digits = 3),
            title = if (completed) {
                localizer.localize("APP.V4_DEPOSIT.COMPLETED_TITLE")
            } else {
                if (status?.error != null) {
                    localizer.localize("ERRORS.API_STATUS.UNKNOWN_API_ERROR")
                } else {
                    localizer.localize("APP.V4_DEPOSIT.IN_PROGRESS_TITLE")
                }
            },
            subtitle = if (completed) {
                localizer.localize("APP.V4_DEPOSIT.COMPLETED_TEXT")
            } else {
                localizer.localizeWithParams(
                    "APP.DEPOSIT_MODAL.YOUR_FUNDS_AVAILABLE_SOON",
                    mapOf(
                        "AMOUNT_ELEMENT" to (formatter.dollar(usdcSize, digits = 2) ?: ""),
                    ),
                )
            },
            status = if (completed) {
                StatusIcon.SUCCESS
            } else {
                if (status?.error != null) {
                    StatusIcon.FAILED
                } else {
                    StatusIcon.SUBMITTING
                }
            },
            closeAction = {
                router.navigateBack()
            },
        )
    }

    private fun routeCompleted(transferStatus: TransferStatus, chainId: String?): Boolean {
        if (transferStatus.squidTransactionStatus == "success") {
            return true
        }
        val statusContainsExecuted = transferStatus.status?.contains("executed") == true
        val lastStatus = transferStatus.routeStatuses?.lastOrNull()
        if (statusContainsExecuted &&
            lastStatus?.chainId == chainId &&
            lastStatus?.status == "success"
        ) {
            return true
        }
        return false
    }

    private fun stopTrackingTransaction() {
        if (transfer != null) {
            transferStateManager.remove(transfer)
        }
        timer?.cancel()
    }
}
