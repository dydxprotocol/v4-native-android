package exchange.dydx.trading.feature.profile.systemstatus

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class DydxSystemStatusViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    init {
        router.navigateBack()
        val url = abacusStateManager.environment?.links?.statusPage ?: abacusStateManager.environment?.links?.community
        if (url != null) {
            router.navigateTo(url)
        }
    }

    val state: Flow<DydxSystemStatusView.ViewState?> = MutableStateFlow(createViewState())

    private fun createViewState(): DydxSystemStatusView.ViewState {
        return DydxSystemStatusView.ViewState(
            localizer = localizer,
        )
    }
}
