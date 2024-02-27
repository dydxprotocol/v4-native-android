package exchange.dydx.trading.feature.market.marketlist.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxMarketSparklineViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketSparklineView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxMarketSparklineView.ViewState {
        return DydxMarketSparklineView.ViewState(
            localizer = localizer,
            sharedMarketViewState = null,
        )
    }
}
