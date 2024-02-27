package exchange.dydx.trading.feature.newsalerts.alerts

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.DydxAlertsProvider
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.DydxAlertsProviderItemProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxAlertsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val alertsProvider: DydxAlertsProvider,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxAlertsView.ViewState?> =
        alertsProvider.items
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(items: List<DydxAlertsProviderItemProtocol>): DydxAlertsView.ViewState {
        return DydxAlertsView.ViewState(
            localizer = localizer,
            items = items.mapNotNull {
                when (it) {
                    is DydxAlertsView.Item -> it
                    else -> null
                }
            },
        )
    }
}
