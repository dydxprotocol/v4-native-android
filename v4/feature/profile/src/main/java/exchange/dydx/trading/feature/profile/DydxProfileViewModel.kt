package exchange.dydx.trading.feature.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxProfileViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxProfileView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxProfileView.ViewState {
        return DydxProfileView.ViewState(
            localizer = localizer,
        )
    }
}
