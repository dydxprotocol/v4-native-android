package exchange.dydx.trading.feature.market.marketinfo.components.depth

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.MarketOrderbook
import exchange.dydx.abacus.output.OrderbookLine
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.charts.config.AxisConfig
import exchange.dydx.platformui.components.charts.config.AxisTextPosition
import exchange.dydx.platformui.components.charts.config.DrawingConfig
import exchange.dydx.platformui.components.charts.config.InteractionConfig
import exchange.dydx.platformui.components.charts.config.LabelConfig
import exchange.dydx.platformui.components.charts.config.LineChartConfig
import exchange.dydx.platformui.components.charts.config.LineChartDrawingConfig
import exchange.dydx.platformui.components.charts.formatter.ValueAxisFormatter
import exchange.dydx.platformui.components.charts.view.LineChartDataSet
import exchange.dydx.platformui.components.textgroups.TextPair
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.absoluteValue

enum class OrderbookSide {
    BIDS,
    ASKS,
}

data class OrderbookLineWithSide(
    val orderbookLine: OrderbookLine,
    val side: OrderbookSide,
)

@HiltViewModel
class DydxMarketDepthViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel, OnChartValueSelectedListener {
    private var selectedOrderbookLine = MutableStateFlow<OrderbookLineWithSide?>(null)

    val state: Flow<DydxMarketDepthView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput.map { it?.marketId },
            abacusStateManager.state.marketMap,
            abacusStateManager.state.orderbooksMap,
            selectedOrderbookLine,
        ) { marketId, marketMap, orderbookMap, selectedOrderbookLine ->
            if (marketId == null) {
                return@combine null
            }
            val market = marketMap?.get(marketId)
            val orderbook = orderbookMap?.get(marketId)
            createViewState(
                market,
                orderbook,
                selectedOrderbookLine,
            )
        }
            .distinctUntilChanged()

    private val config: LineChartConfig = LineChartConfig(
        lineDrawing = LineChartDrawingConfig(
            2.0f,
            exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.text_primary.color.toArgb(),
            0.3f,
            false,
        ),
        drawing = DrawingConfig(
            null,
            true,
        ),
        interaction = InteractionConfig.default.copy(
            selectionListener = this,
        ),
        xAxis = AxisConfig(
            false,
            false,
            LabelConfig(
                ValueAxisFormatter(),
                8.0f,
                exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.text_secondary.color.toArgb(),
                AxisTextPosition.INSIDE,
            ),
        ),
        leftAxis = AxisConfig(
            false,
            false,
            null,
        ),
        rightAxis = null,
    )

    private fun lastAsksWithin2pct(asks: List<Entry>?): Float? {
        val firstAsk = asks?.firstOrNull()
        return if (firstAsk != null) {
            val firstAskValue = firstAsk.x
            asks.lastOrNull {
                (it.x - firstAskValue) / firstAskValue <= 0.02
            }?.x
        } else {
            null
        }
    }

    private fun lastBidsWithin2pct(bids: List<Entry>?): Float? {
        val sortedBids = bids?.reversed()
        val firstBid = sortedBids?.firstOrNull()
        return if (firstBid != null) {
            val firstBidValue = firstBid.x
            sortedBids.lastOrNull {
                (firstBidValue - it.x) / firstBidValue <= 0.02
            }?.x
        } else {
            null
        }
    }

    private fun createViewState(
        market: PerpetualMarket?,
        orderbook: MarketOrderbook?,
        selectedOrderbookLine: OrderbookLineWithSide?
    ): DydxMarketDepthView.ViewState {
        val bids = orderbook?.bids?.let {
            it.reversed().map { orderbookLine ->
                val depth = orderbookLine.depth?.toFloat() ?: 0f
                val price = orderbookLine.price.toFloat()
                val entry = Entry(
                    price,
                    depth,
                )
                entry.data = OrderbookLineWithSide(
                    orderbookLine,
                    OrderbookSide.BIDS,
                )
                entry
            }
        }
        val bidsData = LineChartDataSet(
            bids,
            "Bids",
        )
        val asks = orderbook?.asks?.let {
            it.map { orderbookLine ->
                val depth = orderbookLine.depth!!.toFloat()
                val price = orderbookLine.price.toFloat()
                val entry = Entry(
                    price,
                    depth,
                )
                entry.data = OrderbookLineWithSide(
                    orderbookLine,
                    OrderbookSide.ASKS,
                )
                entry
            }
        }
        val asksData =
            LineChartDataSet(
                asks,
                "Asks",
            )

        val selectedTextPairs = selectedOrderbookLine?.let {
            val orderbookLine = it.orderbookLine
            val side = it.side
            val price = orderbookLine.price
            val depth = orderbookLine.depth
            val priceText = formatter.dollar(price, market?.configs?.tickSizeDecimals ?: 2)
            val depthText = formatter.raw(depth, market?.configs?.stepSizeDecimals ?: 0)
            val slippageText = when (side) {
                OrderbookSide.BIDS -> {
                    orderbook?.bids?.firstOrNull()?.price?.let { firstBid ->
                        val slippage = (firstBid - price).absoluteValue / firstBid
                        formatter.percent(slippage, 2)
                    }
                }
                OrderbookSide.ASKS -> {
                    orderbook?.asks?.firstOrNull()?.price?.let { firstAsk ->
                        val slippage = (price - firstAsk).absoluteValue / firstAsk
                        formatter.percent(slippage, 2)
                    }
                }
            }

            val textPairs = mutableListOf(
                TextPair(
                    localizer.localize("APP.GENERAL.PRICE_CHART_SHORT"),
                    priceText,
                ),
                TextPair(
                    localizer.localize("APP.TRADE.TOTAL_SIZE"),
                    depthText,
                ),
            )
            if (slippageText != null) {
                textPairs.add(
                    TextPair(
                        localizer.localize("APP.TRADE.PRICE_IMPACT"),
                        slippageText,
                    ),
                )
            }
            textPairs
        }
        return DydxMarketDepthView.ViewState(
            localizer,
            config,
            asksData,
            bidsData,
            market?.displayId,
            orderbook?.midPrice?.toFloat(),
            lastBidsWithin2pct(bids),
            lastAsksWithin2pct(asks),
            selectedTextPairs,
        )
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        selectedOrderbookLine.value = e?.data as? OrderbookLineWithSide
    }

    override fun onNothingSelected() {
        selectedOrderbookLine.value = null
    }
}
