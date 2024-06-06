package exchange.dydx.trading.feature.transfer.status

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.TransferStatus
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.transfers.DydxTransferInstance
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.dydxstatemanager.nativeTokenName
import exchange.dydx.dydxstatemanager.usdcTokenName
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.Toast
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.views.ProgressStepView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@HiltViewModel
class DydxTransferStatusViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    private val parser: ParserProtocol,
    savedStateHandle: SavedStateHandle,
    val toaster: PlatformInfo,
) : ViewModel(), DydxViewModel {

    private val transactionHash: String?
    private var timer: Timer? = null

    private val mintscanUrl = abacusStateManager.environment?.links?.mintscan

    init {
        transactionHash = savedStateHandle["hash"]

        if (transactionHash == null) {
            router.navigateBack()
        } else {
            abacusStateManager.state.transferState.take(1)
                .onEach { transferState ->
                    val transfer =
                        transferState?.transfers?.firstOrNull { it.transactionHash == transactionHash }
                    if (transfer != null) {
                        fetchTransferStatuses(transfer)
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    val state: Flow<DydxTransferStatusView.ViewState?> =
        combine(
            abacusStateManager.state.transferState.take(1),
            abacusStateManager.state.transferStatuses,
        ) { transferState, statuses ->
            val transfer = transferState?.transfers?.firstOrNull { it.transactionHash == transactionHash }
            when (transfer?.transferType) {
                DydxTransferInstance.TransferType.DEPOSIT -> createDepositViewState(
                    transfer,
                    statuses,
                )

                DydxTransferInstance.TransferType.WITHDRAWAL -> createWithdrawalViewState(
                    transfer,
                    statuses,
                )

                DydxTransferInstance.TransferType.TRANSFER_OUT -> createTransferOutViewState(
                    transfer,
                )

                else -> null
            }
        }
            .distinctUntilChanged()

    private fun fetchTransferStatuses(transfer: DydxTransferInstance) {
        timer?.cancel()
        timer = fixedRateTimer(initialDelay = 0, period = 30000) {
            abacusStateManager.transferStatus(
                hash = transfer.transactionHash,
                fromChainId = transfer.fromChainId,
                toChainId = transfer.toChainId,
                isCctp = transfer.isCctp ?: false,
                requestId = transfer.requestId,
            )
        }
    }

    private fun createDepositViewState(
        transfer: DydxTransferInstance?,
        statuses: Map<String, TransferStatus>?,
    ): DydxTransferStatusView.ViewState? {
        val status = statuses?.get(transactionHash)
        val routeStatus = routeStatus(transfer, statuses)
        if (routeStatus == RouteStatus.Completed && transfer != null) {
            abacusStateManager.removeTransferInstance(transfer)
        }
        status?.error?.let {
            toaster.show(
                title = localizer.localize("ERRORS.API_STATUS.UNKNOWN_API_ERROR"),
                message = it,
            )
        }

        val size = transfer?.usdcSize ?: 0.0
        return DydxTransferStatusView.ViewState(
            localizer = localizer,
            title = when (routeStatus) {
                RouteStatus.Completed -> localizer.localize("APP.V4_DEPOSIT.COMPLETED_TITLE")
                RouteStatus.InProgress, RouteStatus.Step1 -> localizer.localize("APP.V4_DEPOSIT.IN_PROGRESS_TITLE")
                RouteStatus.NoHash -> localizer.localize("APP.V4_DEPOSIT.CHECK_STATUS_TITLE")
            },
            text = when (routeStatus) {
                RouteStatus.Completed -> localizer.localize("APP.V4_DEPOSIT.COMPLETED_TEXT")
                RouteStatus.InProgress, RouteStatus.Step1 -> localizer.localizeWithParams(
                    path = "APP.V4_DEPOSIT.IN_PROGRESS_TEXT",
                    params = mapOf(
                        "AMOUNT_ELEMENT" to (formatter.dollar(size, 2) ?: ""),
                    ),
                )
                RouteStatus.NoHash -> localizer.localize("APP.V4_DEPOSIT.CHECK_STATUS_TEXT")
            },
            steps = listOf(
                ProgressStepView.ViewState(
                    status = when (routeStatus) {
                        RouteStatus.Completed, RouteStatus.Step1 -> ProgressStepView.Status.Completed
                        RouteStatus.InProgress, RouteStatus.NoHash -> ProgressStepView.Status.InProgress
                    },
                    title = localizer.localize("APP.ONBOARDING.INITIATED_DEPOSIT"),
                    tapAction = if (routeStatus != RouteStatus.NoHash && !status?.fromChainStatus?.transactionUrl.isNullOrEmpty()) {
                        {
                            router.navigateTo(
                                route = status?.fromChainStatus?.transactionUrl!!,
                            )
                        }
                    } else {
                        null
                    },
                    trailingIcon = if (routeStatus != RouteStatus.NoHash && !status?.fromChainStatus?.transactionUrl.isNullOrEmpty()) {
                        R.drawable.icon_external_link
                    } else {
                        null
                    },
                ),
                ProgressStepView.ViewState(
                    status = when (routeStatus) {
                        RouteStatus.Completed -> ProgressStepView.Status.Completed
                        RouteStatus.Step1 -> ProgressStepView.Status.InProgress
                        RouteStatus.InProgress, RouteStatus.NoHash -> ProgressStepView.Status.Custom("2")
                    },
                    title = localizer.localize("APP.ONBOARDING.BRIDGING_TOKENS"),
                    tapAction = if (routeStatus != RouteStatus.NoHash && !status?.axelarTransactionUrl.isNullOrBlank()) {
                        {
                            router.navigateTo(
                                route = status?.axelarTransactionUrl!!,
                            )
                        }
                    } else {
                        null
                    },
                    trailingIcon = if (routeStatus != RouteStatus.NoHash && !status?.axelarTransactionUrl.isNullOrBlank()) {
                        R.drawable.icon_external_link
                    } else {
                        null
                    },
                ),
                ProgressStepView.ViewState(
                    status = when (routeStatus) {
                        RouteStatus.Completed -> ProgressStepView.Status.Completed
                        RouteStatus.InProgress, RouteStatus.Step1, RouteStatus.NoHash -> ProgressStepView.Status.Custom("3")
                    },
                    title = localizer.localize("APP.ONBOARDING.DEPOSIT_TO_DYDX"),
                ),
            ),
            deleteAction = createDeleteAction(transfer, routeStatus),
            closeAction = {
                router.navigateBack()
            },
        )
    }

    private fun createWithdrawalViewState(
        transfer: DydxTransferInstance?,
        statuses: Map<String, TransferStatus>?,
    ): DydxTransferStatusView.ViewState? {
        val status = statuses?.get(transactionHash)
        val routeStatus = routeStatus(transfer, statuses)
        if (routeStatus == RouteStatus.Completed && transfer != null) {
            abacusStateManager.removeTransferInstance(transfer)
        }
        status?.error?.let {
            toaster.show(
                title = localizer.localize("ERRORS.API_STATUS.UNKNOWN_API_ERROR"),
                message = it,
                type = Toast.Type.Error,
            )
        }

        val size = transfer?.usdcSize ?: 0.0
        return DydxTransferStatusView.ViewState(
            localizer = localizer,
            title = when (routeStatus) {
                RouteStatus.Completed -> localizer.localize("APP.V4_WITHDRAWAL.COMPLETED_TITLE")
                RouteStatus.InProgress, RouteStatus.Step1 -> localizer.localize("APP.V4_DEPOSIT.IN_PROGRESS_TITLE")
                RouteStatus.NoHash -> localizer.localize("APP.V4_WITHDRAWAL.CHECK_STATUS_TITLE")
            },
            text = when (routeStatus) {
                RouteStatus.Completed -> localizer.localize("APP.V4_WITHDRAWAL.COMPLETED_TEXT")
                RouteStatus.InProgress, RouteStatus.Step1 -> localizer.localizeWithParams(
                    path = "APP.V4_WITHDRAWAL.IN_PROGRESS_TEXT",
                    params = mapOf(
                        "AMOUNT_ELEMENT" to (formatter.dollar(size, 2) ?: ""),
                    ),
                )
                RouteStatus.NoHash -> localizer.localize("APP.V4_WITHDRAWAL.CHECK_STATUS_TEXT")
            },
            steps = listOf(
                ProgressStepView.ViewState(
                    status = when (routeStatus) {
                        RouteStatus.Completed, RouteStatus.Step1 -> ProgressStepView.Status.Completed
                        RouteStatus.InProgress, RouteStatus.NoHash -> ProgressStepView.Status.InProgress
                    },
                    title = localizer.localize("APP.ONBOARDING.INITIATED_WITHDRAWAL"),
                    tapAction = if (routeStatus != RouteStatus.NoHash && transfer?.transactionHash != null && mintscanUrl != null) {
                        {
                            val hash = transfer.transactionHash.removeRange(0, 2) // remove "0x"
                            val url = mintscanUrl.replace("{tx_hash}", hash)
                            router.navigateTo(
                                route = url,
                            )
                        }
                    } else {
                        null
                    },
                    trailingIcon = if (routeStatus != RouteStatus.NoHash && transfer?.transactionHash != null && mintscanUrl != null) {
                        R.drawable.icon_external_link
                    } else {
                        null
                    },
                ),
                ProgressStepView.ViewState(
                    status = when (routeStatus) {
                        RouteStatus.Completed -> ProgressStepView.Status.Completed
                        RouteStatus.Step1 -> ProgressStepView.Status.InProgress
                        RouteStatus.InProgress, RouteStatus.NoHash -> ProgressStepView.Status.Custom("2")
                    },
                    title = localizer.localize("APP.ONBOARDING.BRIDGING_TOKENS"),
                    tapAction = if (routeStatus != RouteStatus.NoHash && !status?.axelarTransactionUrl.isNullOrEmpty()) {
                        {
                            router.navigateTo(
                                route = status?.axelarTransactionUrl!!,
                            )
                        }
                    } else {
                        null
                    },
                    trailingIcon = if (routeStatus != RouteStatus.NoHash && !status?.axelarTransactionUrl.isNullOrEmpty()) {
                        R.drawable.icon_external_link
                    } else {
                        null
                    },
                ),
                ProgressStepView.ViewState(
                    status = when (routeStatus) {
                        RouteStatus.Completed -> ProgressStepView.Status.Completed
                        RouteStatus.InProgress, RouteStatus.Step1, RouteStatus.NoHash -> ProgressStepView.Status.Custom("3")
                    },
                    title = localizer.localizeWithParams(
                        path = "APP.ONBOARDING.DEPOSIT_TO_DESTINATION",
                        params = mapOf(
                            "DESTINATION_CHAIN" to (transfer?.toChainName ?: ""),
                        ),
                    ),
                ),
            ),
            deleteAction = createDeleteAction(transfer, routeStatus),
            closeAction = {
                router.navigateBack()
            },
        )
    }

    private fun createTransferOutViewState(
        transfer: DydxTransferInstance?
    ): DydxTransferStatusView.ViewState? {
        val params: Map<String, String> =
            if (transfer?.usdcSize != null) {
                mapOf(
                    "AMOUNT_ELEMENT" to (formatter.dollar(transfer.usdcSize, 2) ?: ""),
                    "TOKEN" to abacusStateManager.usdcTokenName,
                )
            } else if (transfer?.size != null) {
                mapOf(
                    "AMOUNT_ELEMENT" to (formatter.raw(transfer.size, 2) ?: ""),
                    "TOKEN" to abacusStateManager.nativeTokenName,
                )
            } else {
                mapOf()
            }
        return DydxTransferStatusView.ViewState(
            localizer = localizer,
            title = localizer.localize("APP.V4_TRANSFEROUT.COMPLETED_TITLE"),
            text = localizer.localizeWithParams("APP.V4_TRANSFEROUT.COMPLETED_TEXT", params),
            steps = listOf(
                ProgressStepView.ViewState(
                    status = ProgressStepView.Status.Completed,
                    title = localizer.localize("APP.ONBOARDING.INITIATED_TRANSFEROUT"),
                    tapAction = if (transfer?.transactionHash != null && mintscanUrl != null) {
                        {
                            val hash = transfer.transactionHash.removeRange(0, 2) // remove "0x"
                            val url = mintscanUrl.replace("{tx_hash}", hash)
                            router.navigateTo(
                                route = url,
                            )
                        }
                    } else {
                        null
                    },
                    trailingIcon = if (transfer?.transactionHash != null && mintscanUrl != null) {
                        R.drawable.icon_external_link
                    } else {
                        null
                    },
                ),
            ),
            deleteAction = {
                if (transfer != null) {
                    abacusStateManager.removeTransferInstance(transfer)
                }
                router.navigateBack()
            },
            closeAction = {
                router.navigateBack()
            },
        )
    }

    private enum class RouteStatus {
        Completed, InProgress, NoHash, Step1;
    }

    private fun routeStatus(
        transfer: DydxTransferInstance?,
        statuses: Map<String, TransferStatus>?,
    ): RouteStatus {
        val status = statuses?.get(transactionHash) ?: return RouteStatus.NoHash

        return if (status.squidTransactionStatus == "success") {
            RouteStatus.Completed
        } else {
            if (status.routeStatuses?.firstOrNull()?.status == "success") {
                RouteStatus.Step1
            } else {
                RouteStatus.InProgress
            }
        }
    }

    private fun createDeleteAction(
        transfer: DydxTransferInstance?,
        routeStatus: RouteStatus,
    ): (() -> Unit)? {
        if (transfer == null) return null

        val ellapsedTime = System.currentTimeMillis() - (transfer.dateIntoEpochMilli ?: 0)

        if (routeStatus == RouteStatus.Completed ||
            ellapsedTime > (1000 * 60 * 60).toLong()
        ) {
            return {
                abacusStateManager.removeTransferInstance(transfer)
                router.navigateBack()
            }
        } else {
            return null
        }
    }
}
