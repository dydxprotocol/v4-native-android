package exchange.dydx.trading.feature.market.marketinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.market.marketinfo.components.tabs.DydxMarketAccountTabView
import exchange.dydx.trading.feature.market.marketinfo.components.tabs.DydxMarketStatsTabView
import exchange.dydx.trading.feature.market.marketinfo.components.tiles.DydxMarketTilesView
import exchange.dydx.trading.feature.market.marketinfo.streams.MutableMarketInfoStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxMarketInfoViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val mutableMarketInfoStream: MutableMarketInfoStreaming,
    statsTabFlow: Flow<@JvmSuppressWildcards DydxMarketStatsTabView.Selection>,
    accountTabFlow: MutableStateFlow<@JvmSuppressWildcards DydxMarketAccountTabView.Selection>,
    tileFlow: Flow<@JvmSuppressWildcards DydxMarketTilesView.Tile>,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), DydxViewModel {

    init {
        mutableMarketInfoStream.update(marketId = savedStateHandle["marketId"])

        val currentSection: String? = savedStateHandle["currentSection"]
        if (currentSection != null) {
            accountTabFlow.value = DydxMarketAccountTabView.Selection.valueOf(currentSection)
        }
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
            statsTabFlow,
            accountTabFlow,
            tileFlow,
        ) { statsTabSelection, accountTabSelection, tileSelection ->
            createViewState(
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
        statsTabSelection: DydxMarketStatsTabView.Selection,
        accountTabSelection: DydxMarketAccountTabView.Selection,
        tileSelection: DydxMarketTilesView.Tile,
        statTabChanged: Boolean,
        accountTabChanged: Boolean,
        tileChanged: Boolean,
    ): DydxMarketInfoView.ViewState {
        return DydxMarketInfoView.ViewState(
            localizer = localizer,
            statsTabSelection = statsTabSelection,
            tileSelection = tileSelection.type,
            accountTabSelection = accountTabSelection,
            scrollToIndex = if (tileChanged) 0 else null,
        )
    }
}
