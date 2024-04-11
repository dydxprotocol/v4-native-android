package exchange.dydx.trading.feature.newsalerts.alerts.alertprovider

import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.providers.DydxFrontendAlertsProvider
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.providers.DydxSystemAlertsProvider
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.providers.DydxTransferAlertsProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class DydxAlertsProvider @Inject constructor(
    val dydxSystemAlertsProvider: DydxSystemAlertsProvider,
    val dydxTransferAlertsProvider: DydxTransferAlertsProvider,
    val dydxFrontEndAlertsProvider: DydxFrontendAlertsProvider,
) : DydxAlertsProviderProtocol {

    override val items: Flow<List<DydxAlertsProviderItemProtocol>> =
        combine(
            dydxSystemAlertsProvider.items,
            dydxTransferAlertsProvider.items,
            dydxFrontEndAlertsProvider.items,
        ) { systemItems, transferItems, frontEndItems ->
            systemItems + transferItems + frontEndItems
        }

    override val showAlertIndicator: Flow<Boolean> =
        combine(
            dydxSystemAlertsProvider.showAlertIndicator,
            dydxTransferAlertsProvider.showAlertIndicator,
            dydxFrontEndAlertsProvider.showAlertIndicator,
        ) { systemAlert, transferAlert, frontEndAlert ->
            systemAlert || transferAlert || frontEndAlert
        }
}
