package exchange.dydx.trading.feature.market.marketlist.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxMarketSummaryViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketSummaryView.ViewState?> = abacusStateManager.state.marketSummary
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(marketSummary: PerpetualMarketSummary?): DydxMarketSummaryView.ViewState {
        val volume = formatter.dollarVolume(marketSummary?.volume24HUSDC)
        val openInterest = formatter.dollarVolume(marketSummary?.openInterestUSDC)
        val trades = formatter.localFormatted(marketSummary?.trades24H, 0)
        return DydxMarketSummaryView.ViewState(
            localizer = localizer,
            items = listOf(
                DydxMarketSummaryView.SummaryItem(
                    header = localizer.localize("APP.TRADE.VOLUME_24H"),
                    value = volume,
                ),
                DydxMarketSummaryView.SummaryItem(
                    header = localizer.localize("APP.TRADE.OPEN_INTEREST"),
                    value = openInterest,
                ),
                DydxMarketSummaryView.SummaryItem(
                    header = localizer.localize("APP.TRADE.TRADES"),
                    value = trades,
                ),
            ),
        )
    }
}
