package exchange.dydx.trading.feature.market.marketinfo.components.paging

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.market.marketinfo.components.tiles.DydxMarketTilesView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxMarketInfoPagingViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val tileFlow: Flow<DydxMarketTilesView.Tile?>,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketInfoPagingView.ViewState?> =
        tileFlow
            .map {
                if (it == null) {
                    return@map null
                }
                createViewState(it.type)
            }
            .distinctUntilChanged()

    private fun createViewState(tileType: DydxMarketTilesView.TileType): DydxMarketInfoPagingView.ViewState {
        return DydxMarketInfoPagingView.ViewState(
            localizer = localizer,
            selection = tileType,
        )
    }
}
