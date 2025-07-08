package exchange.dydx.trading.feature.trade.orderbook

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxOrderbookViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxOrderbookView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxOrderbookView.ViewState {
        return DydxOrderbookView.ViewState(
            localizer = localizer,
        )
    }
}
