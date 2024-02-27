package exchange.dydx.trading.feature.market.marketinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.market.marketinfo.components.tabs.DydxMarketAccountTabView
import exchange.dydx.trading.feature.market.marketinfo.components.tabs.DydxMarketStatsTabView
import exchange.dydx.trading.feature.market.marketinfo.components.tiles.DydxMarketTilesView
import exchange.dydx.trading.feature.market.marketinfo.streams.MarketInfoStreaming
import exchange.dydx.trading.feature.market.marketinfo.streams.MutableMarketInfoStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxMarketInfoViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val mutableMarketInfoStream: MutableMarketInfoStreaming,
    private val marketInfoStream: MarketInfoStreaming,
    private val abacusStateManager: AbacusStateManagerProtocol,
    statsTabFlow: Flow<@JvmSuppressWildcards DydxMarketStatsTabView.Selection>,
    accountTabFlow: Flow<@JvmSuppressWildcards DydxMarketAccountTabView.Selection>,
    tileFlow: Flow<@JvmSuppressWildcards DydxMarketTilesView.Tile>,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), DydxViewModel {

    init {
        mutableMarketInfoStream.update(marketId = savedStateHandle["marketId"])
    }

    override fun onCleared() {
        super.onCleared()

        mutableMarketInfoStream.update(marketId = null)
    }

    private var currentStatsTabSelection: DydxMarketStatsTabView.Selection? = null
    private var currentAccountTabSelection: DydxMarketAccountTabView.Selection? = null
    private var currentTile: DydxMarketTilesView.Tile? = null

    val state: Flow<DydxMarketInfoView.ViewState?> =
        combine(
            marketInfoStream.selectedSubaccountPosition,
            statsTabFlow,
            accountTabFlow,
            tileFlow,
        ) { selectedSubaccountPosition, statsTabSelection, accountTabSelection, tileSelection ->
            createViewState(
                hasPosition = selectedSubaccountPosition != null,
                statsTabSelection = statsTabSelection,
                accountTabSelection = accountTabSelection,
                tileSelection = tileSelection,
                statTabChanged = currentStatsTabSelection != statsTabSelection && currentStatsTabSelection != null,
                accountTabChanged = currentAccountTabSelection != accountTabSelection && currentAccountTabSelection != null,
                tileChanged = currentTile != tileSelection && currentTile != null,
            ).also {
                currentStatsTabSelection = statsTabSelection
                currentAccountTabSelection = accountTabSelection
                currentTile = tileSelection
            }
        }
            .distinctUntilChanged()

    private fun createViewState(
        hasPosition: Boolean,
        statsTabSelection: DydxMarketStatsTabView.Selection,
        accountTabSelection: DydxMarketAccountTabView.Selection,
        tileSelection: DydxMarketTilesView.Tile,
        statTabChanged: Boolean,
        accountTabChanged: Boolean,
        tileChanged: Boolean,
    ): DydxMarketInfoView.ViewState {
        return DydxMarketInfoView.ViewState(
            localizer = localizer,
            hasPosition = hasPosition,
            statsTabSelection = statsTabSelection,
            tileSelection = tileSelection.type,
            accountTabSelection = accountTabSelection,
            scrollToIndex = if (tileChanged) 0 else null,
        )
    }
}
