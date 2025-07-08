package exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.providers

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.transfers.DydxTransferInstance
import exchange.dydx.dydxstatemanager.clientState.transfers.DydxTransferStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.dydxstatemanager.nativeTokenName
import exchange.dydx.dydxstatemanager.usdcTokenName
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.TransferRoutes
import exchange.dydx.trading.feature.newsalerts.alerts.DydxAlertsView
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.AlertType
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.DydxAlertsProviderItemProtocol
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.DydxCustomAlertsProviderProtocol
import exchange.dydx.trading.feature.shared.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class DydxTransferAlertsProvider @Inject constructor(
    transferStateManager: DydxTransferStateManagerProtocol,
    private val abacusStateManger: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val localizer: LocalizerProtocol,
    private val formatter: DydxFormatter,
) : DydxCustomAlertsProviderProtocol {

    override val alertType: AlertType = AlertType.Transfer

    override val items: Flow<List<DydxAlertsProviderItemProtocol>> =
        transferStateManager.state
            .map {
                createAlertItems(it?.transfers) ?: emptyList()
            }
            .distinctUntilChanged()

    override val showAlertIndicator: Flow<Boolean> = flowOf(false)

    private fun createAlertItems(transfers: List<DydxTransferInstance>?): List<DydxAlertsProviderItemProtocol>? {
        return transfers?.map { transfer ->
            val usdcSize = formatter.dollar(transfer.usdcSize, 2) ?: ""
            val size = formatter.raw(transfer.size, 2) ?: ""

            val tapAction: (() -> Unit) = {
                router.navigateTo(
                    route = TransferRoutes.transfer_status + "/${transfer.transactionHash}",
                    presentation = DydxRouter.Presentation.Modal,
                )
            }
            when (transfer.transferType) {
                DydxTransferInstance.TransferType.DEPOSIT -> {
                    DydxAlertsView.Item(
                        title = localizer.localizeWithParams(
                            path = "APP.ONBOARDING.DEPOSIT_ALERT_TITLE",
                            params = mapOf(
                                "PENDING_DEPOSITS" to usdcSize,
                                "SOURCE_CHAIN" to (transfer.fromChainName ?: ""),
                            ),
                        ),
                        message = null,
                        icon = R.drawable.icon_transfer_deposit,
                        tapAction = tapAction,
                        date = Date(),
                    )
                }

                DydxTransferInstance.TransferType.WITHDRAWAL -> {
                    DydxAlertsView.Item(
                        title = localizer.localizeWithParams(
                            path = "APP.ONBOARDING.WITHDRAWAL_ALERT_TITLE",
                            params = mapOf(
                                "PENDING_WITHDRAWALS" to usdcSize,
                                "DESTINATION_CHAIN" to (transfer.toChainName ?: ""),
                            ),
                        ),
                        message = null,
                        icon = R.drawable.icon_transfer_withdrawal,
                        tapAction = tapAction,
                        date = Date(),
                    )
                }

                DydxTransferInstance.TransferType.TRANSFER_OUT -> {
                    val tokenName =
                        if (usdcSize.isNotEmpty()) abacusStateManger.usdcTokenName else abacusStateManger.nativeTokenName
                    DydxAlertsView.Item(
                        title = localizer.localizeWithParams(
                            path = "APP.ONBOARDING.TRANSFEROUT_ALERT_TITLE",
                            params = mapOf(
                                "PENDING_TRANSFERS" to if (usdcSize.isNotEmpty()) usdcSize else size,
                                "TOKEN" to tokenName,
                                "SOURCE_CHAIN" to (transfer.fromChainName ?: ""),
                            ),
                        ),
                        message = null,
                        icon = R.drawable.icon_transfer_out,
                        tapAction = tapAction,
                        date = Date(),
                    )
                }
            }
        }
    }
}
