package exchange.dydx.trading.feature.newsalerts.news

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxNewsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxNewsView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxNewsView.ViewState {
        return DydxNewsView.ViewState(
            localizer = localizer,
            url = abacusStateManager.environment?.links?.blogs,
        )
    }
}
