package exchange.dydx.trading.feature.trade.tradestatus

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxTradeStatusViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
) : ViewModel(), DydxViewModel {
    val state: Flow<DydxTradeStatusView.ViewState?> = flowOf(createViewState())
        .distinctUntilChanged()

    private fun createViewState(): DydxTradeStatusView.ViewState {
        return DydxTradeStatusView.ViewState(
            localizer = localizer,
        )
    }
}
