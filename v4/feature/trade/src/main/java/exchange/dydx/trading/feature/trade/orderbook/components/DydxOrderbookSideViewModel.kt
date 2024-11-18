package exchange.dydx.trading.feature.trade.orderbook.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.MarketOrderbook
import exchange.dydx.abacus.output.OrderbookLine
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.OrderbookUsage
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.utils.IList
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.utilities.utils.timerFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Date
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

private val maxLinesToDisplay = 12 // no need to do extra processing. Even on biggest screen, max displayed is ~10

@HiltViewModel
class DydxOrderbookAsksViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    private val colorMap: MutableMap<Double, ColorMapEntry> = mutableMapOf()

    val state: Flow<DydxOrderbookSideView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput,
            abacusStateManager.state.marketMap,
            abacusStateManager.state.orderbooksMap,
            timer,
        ) { tradeInput, marketMap, orderbookMap, timer ->
            if (tradeInput == null) {
                return@combine null
            }
            val marketId = tradeInput.marketId ?: return@combine null
            val side = tradeInput.side
            val orderbook = orderbookMap?.get(marketId)
            val orderbookUsage = tradeInput.marketOrder?.orderbook
            val asks = orderbook?.asks?.take(maxLinesToDisplay)
            val bids = orderbook?.bids?.take(maxLinesToDisplay)
            createViewState(
                localizer = localizer,
                formatter = formatter,
                market = marketMap?.get(marketId),
                orderbook = orderbook,
                orderbookUsage = if (side == OrderSide.Buy) orderbookUsage else null,
                lines = asks ?: emptyList(),
                maxDepth = maxDepth(bids, asks) ?: 0.0,
                startingColor = ThemeColor.SemanticColor.negativeColor,
                side = DydxOrderbookSideView.Side.Asks,
                colorMap = colorMap,
                onTap = { line ->
                    lineSelected(abacusStateManager, tradeInput, line)
                },
            )
        }
            .distinctUntilChanged()
}

@HiltViewModel
class DydxOrderbookBidsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    private val colorMap: MutableMap<Double, ColorMapEntry> = mutableMapOf()

    val state: Flow<DydxOrderbookSideView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput,
            abacusStateManager.state.marketMap,
            abacusStateManager.state.orderbooksMap,
            timer,
        ) { tradeInput, marketMap, orderbookMap, timer ->
            if (tradeInput == null) {
                return@combine null
            }
            val marketId = tradeInput.marketId ?: return@combine null
            val side = tradeInput.side
            val orderbook = orderbookMap?.get(marketId)
            val orderbookUsage = tradeInput.marketOrder?.orderbook
            val asks = orderbook?.asks?.take(maxLinesToDisplay)
            val bids = orderbook?.bids?.take(maxLinesToDisplay)
            createViewState(
                localizer = localizer,
                formatter = formatter,
                market = marketMap?.get(marketId),
                orderbook = orderbook,
                orderbookUsage = if (side == OrderSide.Sell) orderbookUsage else null,
                lines = bids?.toList() ?: emptyList(),
                maxDepth = maxDepth(bids, asks) ?: 0.0,
                startingColor = ThemeColor.SemanticColor.positiveColor,
                side = DydxOrderbookSideView.Side.Bids,
                colorMap = colorMap,
                onTap = { line ->
                    lineSelected(abacusStateManager, tradeInput, line)
                },
            )
        }
            .distinctUntilChanged()
}

private val colorChangeTime = 0.2.seconds // time to change the color of the new entries
private val timer = timerFlow(period = colorChangeTime)

private data class ColorMapEntry(
    val date: Date,
    val size: Double,
)

private fun createViewState(
    localizer: LocalizerProtocol,
    formatter: DydxFormatter,
    market: PerpetualMarket?,
    orderbook: MarketOrderbook?,
    orderbookUsage: IList<OrderbookUsage>?,
    lines: List<OrderbookLine>,
    maxDepth: Double,
    startingColor: ThemeColor.SemanticColor,
    side: DydxOrderbookSideView.Side,
    colorMap: MutableMap<Double, ColorMapEntry>,
    onTap: (DydxOrderbookSideView.DydxOrderbookLine) -> Unit,
): DydxOrderbookSideView.ViewState {
    val items = lines.take(12)
    val output: MutableList<DydxOrderbookSideView.DydxOrderbookLine> = mutableListOf()
    items.forEach { line ->
        val textColor: ThemeColor.SemanticColor
        if (colorMap.containsKey(line.price)) {
            val entry = colorMap[line.price]!!
            val timeSinceLastChange = Date().time - entry.date.time
            if (timeSinceLastChange < colorChangeTime.inWholeMilliseconds) {
                textColor = startingColor
            } else if (entry.size != line.size) {
                textColor = startingColor
                colorMap[line.price] = ColorMapEntry(Date(), line.size)
            } else {
                textColor = ThemeColor.SemanticColor.text_secondary
            }
        } else {
            textColor = startingColor
            colorMap[line.price] = ColorMapEntry(Date(), line.size)
        }

        val usage = orderbookUsage?.firstOrNull { it.price == line.price }

        output.add(
            DydxOrderbookSideView.DydxOrderbookLine(
                price = line.price,
                size = line.size,
                sizeText = formatter.raw(line.size, market?.configs?.displayStepSizeDecimals ?: 4) ?: "",
                priceText = formatter.dollar(line.price, orderbook?.grouping?.tickSize) ?: "",
                depth = line.depth,
                taken = usage?.size,
                textColor = textColor,
            ),
        )
    }

    return DydxOrderbookSideView.ViewState(
        localizer = localizer,
        lines = output,
        maxDepth = maxDepth,
        side = side,
        onTap = onTap,
    )
}

internal fun lineSelected(
    abacusStateManager: AbacusStateManagerProtocol,
    tradeInput: TradeInput?,
    line: DydxOrderbookSideView.DydxOrderbookLine
) {
    when (tradeInput?.type) {
        OrderType.Limit, OrderType.StopLimit, OrderType.TakeProfitLimit -> {
            abacusStateManager.trade("${line.price}", TradeInputField.limitPrice)
        }

        else -> {}
    }
}

private fun maxDepth(bids: List<OrderbookLine>?, asks: List<OrderbookLine>?): Double {
    val bidsMaxDepth = bids?.maxOfOrNull { it.depth ?: 0.0 } ?: 0.0
    val asksMaxDepth = asks?.maxOfOrNull { it.depth ?: 0.0 } ?: 0.0
    return maxOf(bidsMaxDepth, asksMaxDepth)
}
