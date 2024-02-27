package exchange.dydx.trading.feature.market.marketlist.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxMarketAssetItemViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketAssetItemView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxMarketAssetItemView.ViewState {
        return DydxMarketAssetItemView.ViewState(
            localizer = localizer,
            sharedMarketViewState = null,
        )
    }
}
