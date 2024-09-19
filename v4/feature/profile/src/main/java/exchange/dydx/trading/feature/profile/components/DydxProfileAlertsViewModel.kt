package exchange.dydx.trading.feature.profile.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.ProfileRoutes
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.DydxAlertsProvider
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.DydxAlertsProviderItemProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxProfileAlertsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val alertsProvider: DydxAlertsProvider,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxProfileAlertsView.ViewState?> =
        alertsProvider.items
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        items: List<DydxAlertsProviderItemProtocol>
    ): DydxProfileAlertsView.ViewState {
        return DydxProfileAlertsView.ViewState(
            localizer = localizer,
            hasAlerts = items.isNotEmpty(),
            tapAction = {
                router.navigateTo(
                    route = ProfileRoutes.alerts,
                    presentation = DydxRouter.Presentation.Push,
                )
            },
        )
    }
}
