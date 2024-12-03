package exchange.dydx.trading.feature.market.marketinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.market.marketinfo.components.tabs.DydxMarketAccountTabView
import exchange.dydx.trading.feature.market.marketinfo.components.tabs.DydxMarketStatsTabView
import exchange.dydx.trading.feature.market.marketinfo.components.tiles.DydxMarketTilesView
import exchange.dydx.trading.feature.market.marketinfo.streams.MutableMarketInfoStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    private val scrollToTopFlow = MutableStateFlow<Boolean>(false)

    init {
        mutableMarketInfoStream.update(marketId = savedStateHandle["marketId"])

        val currentSection: String? = savedStateHandle["currentSection"]
        if (currentSection != null) {
            accountTabFlow.value = DydxMarketAccountTabView.Selection.valueOf(currentSection)
        }

        tileFlow
            .distinctUntilChanged()
            .onEach {
                // trigger scroll to top
                scrollToTopFlow.value = true
                CoroutineTimer.instance.schedule(0.1, repeat = null) {
                    scrollToTopFlow.value = false
                    return@schedule false
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()

        mutableMarketInfoStream.update(marketId = null)
    }

    val state: Flow<DydxMarketInfoView.ViewState?> =
        combine(
            statsTabFlow,
            accountTabFlow,
            tileFlow,
            scrollToTopFlow,
        ) { statsTabSelection, accountTabSelection, tileSelection, scrollToTop ->
            createViewState(
                statsTabSelection = statsTabSelection,
                accountTabSelection = accountTabSelection,
                tileSelection = tileSelection,
                scrollToTop = scrollToTop,
            )
        }
            .distinctUntilChanged()

    private fun createViewState(
        statsTabSelection: DydxMarketStatsTabView.Selection,
        accountTabSelection: DydxMarketAccountTabView.Selection,
        tileSelection: DydxMarketTilesView.Tile,
        scrollToTop: Boolean,
    ): DydxMarketInfoView.ViewState {
        return DydxMarketInfoView.ViewState(
            localizer = localizer,
            statsTabSelection = statsTabSelection,
            tileSelection = tileSelection.type,
            accountTabSelection = accountTabSelection,
            scrollToIndex = if (scrollToTop) 0 else null,
        )
    }
}
