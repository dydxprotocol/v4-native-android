package exchange.dydx.trading.feature.newsalerts.alerts.alertprovider

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Date

enum class AlertType {
    System, Transfer, FrontEnd
}

interface DydxCustomAlertsProviderProtocol : DydxAlertsProviderProtocol {
    val alertType: AlertType
}

interface DydxAlertsProviderProtocol {
    val items: Flow<List<DydxAlertsProviderItemProtocol>>
    val showAlertIndicator: Flow<Boolean>
}

interface DydxAlertsProviderItemProtocol {
    val title: String?
    val message: String?
    val icon: Any?
    val tapAction: (() -> Unit)?
    val date: Date?
}

open class DydxBaseAlertsProvider : DydxAlertsProviderProtocol {
    val _items: MutableStateFlow<List<DydxAlertsProviderItemProtocol>> = MutableStateFlow(emptyList())
    val _showAlertIndicator: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val items: Flow<List<DydxAlertsProviderItemProtocol>> = _items
    override val showAlertIndicator: Flow<Boolean> = _showAlertIndicator
}
