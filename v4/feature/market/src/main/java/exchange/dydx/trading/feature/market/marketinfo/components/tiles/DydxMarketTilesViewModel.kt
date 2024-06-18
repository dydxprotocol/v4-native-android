package exchange.dydx.trading.feature.market.marketinfo.components.tiles

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxMarketTilesViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val tileFlow: MutableStateFlow<DydxMarketTilesView.Tile>,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketTilesView.ViewState?> =
        combine(
            abacusStateManager.state.currentWallet,
            tileFlow,
        ) { currentWallet, tile ->
            createViewState(currentWallet, tile)
        }
            .distinctUntilChanged()

    private fun createViewState(
        currentWallet: DydxWalletInstance?,
        currentTile: DydxMarketTilesView.Tile,
    ): DydxMarketTilesView.ViewState {
        val tiles = listOfNotNull(
            if (currentWallet != null) DydxMarketTilesView.Tile(DydxMarketTilesView.TileType.ACCOUNT) else null,
            DydxMarketTilesView.Tile(DydxMarketTilesView.TileType.PRICE),
            DydxMarketTilesView.Tile(DydxMarketTilesView.TileType.DEPTH),
            DydxMarketTilesView.Tile(DydxMarketTilesView.TileType.FUNDING),
            DydxMarketTilesView.Tile(DydxMarketTilesView.TileType.ORDERBOOK),
            DydxMarketTilesView.Tile(DydxMarketTilesView.TileType.RECENT),
        )

        return DydxMarketTilesView.ViewState(
            localizer = localizer,
            tiles = tiles,
            currentSelection = currentTile,
            selectionChangedAction = { tile ->
                tileFlow.value = tile
            },
        )
    }
}
