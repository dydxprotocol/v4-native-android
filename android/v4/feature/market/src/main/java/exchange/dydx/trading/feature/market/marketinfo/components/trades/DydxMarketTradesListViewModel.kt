package exchange.dydx.trading.feature.market.marketinfo.components.trades

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.MarketTrade
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DydxMarketTradesListViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketTradesListView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput.map { it?.marketId },
            abacusStateManager.state.marketMap,
            abacusStateManager.state.tradesMap,
        ) { marketId, marketMap, tradesMap ->
            if (marketId == null) {
                return@combine null
            }
            val market = marketMap?.get(marketId)
            val trades = tradesMap?.get(marketId)
            createViewState(
                trades,
                market,
            )
        }
            .distinctUntilChanged()

    val headerState: DydxMarketTradesHeaderView.ViewState? =
        createHeaderViewState()

    private fun createViewState(
        trades: List<MarketTrade>?,
        market: PerpetualMarket?,
    ): DydxMarketTradesListView.ViewState {
        return trades?.takeUnless { it.isEmpty() }?.let { trades ->
            val max = trades.maxOf { it.size }
            val buy = DydxMarketTradeItemView.SideState(
                barColor = ThemeColor.SemanticColor.positiveColor.color.copy(alpha = 0.2f),
                textColor = ThemeColor.SemanticColor.positiveColor,
                text = localizer.localize("APP.GENERAL.BUY"),
            )
            val sell = DydxMarketTradeItemView.SideState(
                barColor = ThemeColor.SemanticColor.negativeColor.color.copy(alpha = 0.2f),
                textColor = ThemeColor.SemanticColor.negativeColor,
                text = localizer.localize("APP.GENERAL.SELL"),
            )
            DydxMarketTradesListView.ViewState(
                trades = trades.map { trade ->
                    val time = Instant.ofEpochMilli(trade.createdAtMilliseconds.toLong())
                    DydxMarketTradeItemView.ViewState(
                        id = trade.id!!,
                        time = formatter.clock(time),
                        side = if (trade.side == OrderSide.Buy) buy else sell,
                        price = formatter.dollar(trade.price, market?.configs?.tickSizeDecimals ?: 2) ?: "",
                        size = formatter.raw(trade.size, market?.configs?.stepSizeDecimals ?: 2) ?: "",
                        percent = if (max > 0) trade.size / max else 0.0,
                    )
                },
            )
        } ?: DydxMarketTradesListView.ViewState(
            trades = listOf(),
        )
    }

    private fun createHeaderViewState(): DydxMarketTradesHeaderView.ViewState {
        return DydxMarketTradesHeaderView.ViewState(
            localizer.localize("APP.GENERAL.TIME"),
            localizer.localize("APP.GENERAL.SIDE"),
            localizer.localize("APP.GENERAL.PRICE"),
            localizer.localize("APP.GENERAL.SIZE"),
        )
    }
}
