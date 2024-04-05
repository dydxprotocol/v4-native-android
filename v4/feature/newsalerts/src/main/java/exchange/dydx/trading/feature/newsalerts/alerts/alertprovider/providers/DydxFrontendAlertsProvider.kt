package exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.providers

import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.newsalerts.alerts.DydxAlertsView
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.AlertType
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.DydxAlertsProviderItemProtocol
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.DydxCustomAlertsProviderProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class DydxFrontendAlertsProvider @Inject constructor(
    private val abacusStateManger: AbacusStateManagerProtocol,
    private val router: DydxRouter,
) : DydxCustomAlertsProviderProtocol {

    override val alertType: AlertType = AlertType.FrontEnd

    override val items: Flow<List<DydxAlertsProviderItemProtocol>> =
        abacusStateManger.state.alerts.map {
            createAlertItems(it) ?: emptyList()
        }
            .distinctUntilChanged()

    override val showAlertIndicator: Flow<Boolean> = flowOf(false)

    private fun createAlertItems(alerts: List<exchange.dydx.abacus.output.Notification>?): List<DydxAlertsProviderItemProtocol>? {
        return alerts?.map { alert ->
            val tapAction: (() -> Unit) = {
                alert.link?.let {
                    router.navigateTo(
                        route = it,
                        presentation = DydxRouter.Presentation.Modal,
                    )
                }
            }
            DydxAlertsView.Item(
                title = alert.title,
                message = alert.text,
                icon = alert.image,
                tapAction = tapAction,
                date = Date(),
            )
        }
    }
}
