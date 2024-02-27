package exchange.dydx.trading.feature.profile.actions.debugenabled

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.utilities.utils.DebugEnabled
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxDebugEnableViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val sharedPreferencesStore: SharedPreferencesStore,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxDebugEnableView.ViewState?> = flowOf(createViewState())

    init {
        DebugEnabled.update(sharedPreferencesStore, true)
    }

    private fun createViewState(): DydxDebugEnableView.ViewState {
        return DydxDebugEnableView.ViewState(
            localizer = localizer,
        )
    }
}
