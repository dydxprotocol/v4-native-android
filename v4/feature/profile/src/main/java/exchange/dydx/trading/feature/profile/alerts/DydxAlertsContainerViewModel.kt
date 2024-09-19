package exchange.dydx.trading.feature.profile.alerts

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxAlertsContainerViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxAlertsContainerView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxAlertsContainerView.ViewState {
        return DydxAlertsContainerView.ViewState(
            localizer = localizer,
            backButtionAction = {
                router.navigateBack()
            },
        )
    }
}
