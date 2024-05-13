package exchange.dydx.trading.feature.market.marketinfo.components.prices

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.hoc081098.flowext.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.MarketCandle
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.charts.config.AxisConfig
import exchange.dydx.platformui.components.charts.config.AxisTextPosition
import exchange.dydx.platformui.components.charts.config.BarDrawingConfig
import exchange.dydx.platformui.components.charts.config.CandlesDrawingConfig
import exchange.dydx.platformui.components.charts.config.CombinedChartConfig
import exchange.dydx.platformui.components.charts.config.DrawingConfig
import exchange.dydx.platformui.components.charts.config.InteractionConfig
import exchange.dydx.platformui.components.charts.config.LabelConfig
import exchange.dydx.platformui.components.charts.config.LineChartDrawingConfig
import exchange.dydx.platformui.components.charts.view.LineChartDataSet
import exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

private const val TAG = "DydxMarketPricesViewModel"

@HiltViewModel
class DydxMarketPricesViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    @CoroutineScopes.ViewModel private val viewModelScope: CoroutineScope,
    private val logger: Logging,
) : ViewModel(), DydxViewModel, OnChartValueSelectedListener {
    /*
    The library works well with x range up to 1000
    Since we use time interval from anchor time (now), so
    the x for 12AM today has time interval of 0, which we
    want to shift to close to 1000, we use offset of 900
    and add to x value
     */
    private val typeTitles = listOf(
        localizer.localize("APP.GENERAL.CANDLES"),
        localizer.localize("APP.GENERAL.LINE"),
    )
    private val typeIndex = MutableStateFlow(0)
    private val candlesPeriods = listOf(
        "1MIN",
        "5MINS",
        "15MINS",
        "30MINS",
        "1HOUR",
        "4HOURS",
        "1DAY",
    )
    private val defaultResolution = candlesPeriods.indexOf("1HOUR")
    private val resolutionTitles = listOf(
        localizer.localize("APP.GENERAL.TIME_STRINGS.1MIN"),
        localizer.localize("APP.GENERAL.TIME_STRINGS.5MIN"),
        localizer.localize("APP.GENERAL.TIME_STRINGS.15MIN"),
        localizer.localize("APP.GENERAL.TIME_STRINGS.30MIN"),
        localizer.localize("APP.GENERAL.TIME_STRINGS.1H"),
        localizer.localize("APP.GENERAL.TIME_STRINGS.4H"),
        localizer.localize("APP.GENERAL.TIME_STRINGS.1D"),
    )
    private val resolutionIndex = MutableStateFlow(defaultResolution)
    private val selectedPrice = MutableStateFlow<MarketCandle?>(null)

    private val offset = 900

    private val anchorDateTime: Instant = run {
        val now = Instant.now()
        now.truncatedTo(ChronoUnit.DAYS)
        now
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<DydxMarketPricesView.ViewState?> =
        combine(
            abacusStateManager.marketId.filterNotNull().flatMapLatest { abacusStateManager.state.market(it).filterNotNull().distinctUntilChanged() },
            abacusStateManager.marketId.filterNotNull().flatMapLatest { abacusStateManager.state.candles(it).filterNotNull().distinctUntilChanged() },
            abacusStateManager.marketId
                .filterNotNull()
                .flatMapLatest { abacusStateManager.state.selectedSubaccountOrdersOfMarket(it) },
            selectedPrice,
            typeIndex,
            resolutionIndex,
        ) { market, allPrices, ordersForMarket, selectedPrice, typeIndex, resolutionIndex ->
            market.configs?.let { configs ->
                val candlesPeriod = candlesPeriods[resolutionIndex]
                val prices = allPrices.candles?.get(candlesPeriod)
                val orderData = ordersForMarket?.let { orders ->
                    orders
                        .filter {
                            it.status in setOf(OrderStatus.open, OrderStatus.untriggered, OrderStatus.partiallyFilled) &&
                                it.type in setOf(OrderType.limit, OrderType.stopLimit, OrderType.stopMarket, OrderType.takeProfitLimit, OrderType.takeProfitMarket)
                        }
                        .map {
                            OrderData(
                                price = it.price,
                                side = it.side,
                                size = it.remainingSize ?: it.size,
                                formattedPrice = formatter.dollar(it.price, configs.tickSizeDecimals)
                                    ?: run {
                                        logger.e(TAG, "Failed to format orderline price.")
                                        ""
                                    },
                                orderType = it.type,
                            )
                        }
                }.orEmpty()

                createViewState(
                    prices = prices,
                    market = market,
                    orderData = orderData,
                    candlesPeriod = candlesPeriod,
                    selectedPrice = selectedPrice,
                    typeIndex = typeIndex,
                    resolutionIndex = resolutionIndex,
                )
            }
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        abacusStateManager.setCandlesPeriod(candlesPeriods[defaultResolution])
    }

    private fun createViewState(
        prices: List<MarketCandle>?,
        market: PerpetualMarket?,
        orderData: List<OrderData>,
        candlesPeriod: String,
        selectedPrice: MarketCandle?,
        typeIndex: Int,
        resolutionIndex: Int,
    ): DydxMarketPricesView.ViewState {
        val candles = mutableListOf<CandleEntry>()
        val volumes = mutableListOf<BarEntry>()
        val lines = mutableListOf<Entry>()
        var leftPadded = false
        var x = 0f
        prices?.forEach { it ->
            x = reduce(it.startedAtMilliseconds, candlesPeriod).toFloat()
            val candleEntry = CandleEntry(
                /* x = */ x,
                /* shadowH = */ it.high.toFloat(),
                /* shadowL = */ it.low.toFloat(),
                /* open = */ it.open.toFloat(),
                /* close = */ it.close.toFloat(),
            )
            candleEntry.data = it
            candles.add(candleEntry)

            if (!leftPadded) {
                // Add padding. Otherwise, the first bar is cut off in the middle
                padLeft(volumes, x)
                leftPadded = true
            }
            val barEntry = BarEntry(
                /* x = */ x,
                /* y = */ it.usdVolume.toFloat(),
            )
            barEntry.data = it
            volumes.add(barEntry)

            val lineEntry = Entry(
                /* x = */ x,
                /* y = */ it.close.toFloat(),
            )
            lineEntry.data = it
            lines.add(lineEntry)
        }
        if (leftPadded) {
            // Add padding. Otherwise, the last bar is cut off in the middle
            padRight(volumes, x)
        }
        val highlight = selectedPrice?.let { it ->
            val datetimeText = formatter.dateTime(Instant.ofEpochMilli(it.startedAtMilliseconds.toLong()))
            val openText = formatter.dollar(it.open, market?.configs?.tickSizeDecimals ?: 2)
            val closeText = formatter.dollar(it.close, market?.configs?.tickSizeDecimals ?: 2)
            val highText = formatter.dollar(it.high, market?.configs?.tickSizeDecimals ?: 2)
            val lowText = formatter.dollar(it.low, market?.configs?.tickSizeDecimals ?: 2)
            val volumeText = formatter.dollar(it.usdVolume, market?.configs?.tickSizeDecimals ?: 2)

            if (datetimeText != null &&
                openText != null &&
                closeText != null &&
                highText != null &&
                lowText != null &&
                volumeText != null
            ) {
                PriceHighlight(
                    datetimeText,
                    openText,
                    highText,
                    lowText,
                    closeText,
                    volumeText,
                )
            } else {
                null
            }
        }

        val orderData = listOf(
            OrderData(
                price = 3100.0,
                side = OrderSide.sell,
                formattedPrice = "$3100.00",
                orderType = OrderType.takeProfitLimit,
                size = 10.0,
            ),
            OrderData(
                price = 3300.0,
                side = OrderSide.sell,
                formattedPrice = "$3300.00",
                orderType = OrderType.takeProfitMarket,
                size = 10.0,
            ),
            OrderData(
                price = 3500.0,
                side = OrderSide.sell,
                formattedPrice = "$3500.00",
                orderType = OrderType.limit,
                size = 10.0,
            ),
            OrderData(
                price = 2900.0,
                side = OrderSide.buy,
                formattedPrice = "$2900.00",
                orderType = OrderType.stopMarket,
                size = 10.0,
            ),
            OrderData(
                price = 2700.0,
                side = OrderSide.buy,
                formattedPrice = "$2700.00",
                orderType = OrderType.stopLimit,
                size = 10.0,
            ),
        )

        return DydxMarketPricesView.ViewState(
            localizer = localizer,
            config = config(market, candlesPeriod),
            market = market?.id,
            candles = CandleDataSet(candles, "candles"),
            volumes = BarDataSet(volumes, "volumes"),
            prices = LineChartDataSet(lines, "lines"),
            orderLines = orderData,
            typeOptions = SelectionOptions(
                typeTitles,
                typeIndex,
                onChanged = {
                    this.typeIndex.value = it
                },
            ),
            resolutionOptions = SelectionOptions(
                resolutionTitles,
                resolutionIndex,
                onChanged = {
                    this.resolutionIndex.value = it
                    abacusStateManager.setCandlesPeriod(candlesPeriods[it])
                },
            ),
            highlight = highlight,
        )
    }

    private fun padLeft(volumes: MutableList<BarEntry>, x: Float) {
        for (i in 0 until 3) {
            volumes.add(
                BarEntry(
                    x - 3 + i,
                    0f,
                ),
            )
        }
    }

    private fun padRight(volumes: MutableList<BarEntry>, x: Float) {
        for (i in 0 until 3) {
            volumes.add(
                BarEntry(
                    x + i,
                    0f,
                ),
            )
        }
    }

    private fun reduce(milliSeconds: Double, candlesPeriod: String?): Long {
        val time = Instant.ofEpochSecond((milliSeconds / 1000).toLong())
        return when (candlesPeriod) {
            "1DAY" -> {
                anchorDateTime.until(time, ChronoUnit.DAYS)
            }

            "1HOUR" -> {
                anchorDateTime.until(time, ChronoUnit.HOURS)
            }

            "4HOURS" -> {
                anchorDateTime.until(time, ChronoUnit.HOURS) / 4
            }

            "1MIN" -> {
                anchorDateTime.until(time, ChronoUnit.MINUTES)
            }

            "5MINS" -> {
                anchorDateTime.until(time, ChronoUnit.MINUTES) / 5
            }

            "15MINS" -> {
                anchorDateTime.until(time, ChronoUnit.MINUTES) / 15
            }

            "30MINS" -> {
                anchorDateTime.until(time, ChronoUnit.MINUTES) / 30
            }

            // "1MIN", "5MINS", "15MINS", "30MINS",
            else -> {
                anchorDateTime.until(time, ChronoUnit.MINUTES)
            }
        } + offset
    }

    private fun config(market: PerpetualMarket?, candlesPeriod: String?): CombinedChartConfig {
        return CombinedChartConfig(
            candlesDrawing = CandlesDrawingConfig(
                increasingColor = SemanticColor.positiveColor.color.toArgb(),
                decreasingColor = SemanticColor.negativeColor.color.toArgb(),
                neutralColor = SemanticColor.text_primary.color.toArgb(),
            ),
            barDrawing = BarDrawingConfig(
                borderColor = SemanticColor.layer_6.color.toArgb(),
                fillColor = SemanticColor.layer_6.color.toArgb(),
            ),
            lineDrawing = LineChartDrawingConfig(
                lineWidth = 2.0f,
                lineColor = SemanticColor.text_secondary.color.toArgb(),
                fillAlpha = null,
            ),
            drawing = DrawingConfig(
                margin = null,
                autoScale = true,
            ),
            interaction = InteractionConfig.default.copy(
                selectionListener = this,
            ),
            xAxis = AxisConfig(
                drawLine = true,
                drawGrid = false,
                label = LabelConfig(
                    formatter = DateTimeAxisFormatter(
                        anchorDateTime = anchorDateTime,
                        candlesPeriod = candlesPeriod,
                        offset = offset,
                    ),
                    size = 8.0f,
                    color = SemanticColor.text_secondary.color.toArgb(),
                    position = AxisTextPosition.OUTSIDE,
                ),
            ),
            leftAxis = AxisConfig(
                drawLine = false,
                drawGrid = false,
                label = LabelConfig(
                    formatter = PriceAxisFormatter(
                        formatter = formatter,
                        tickSizeDecimals = market?.configs?.tickSizeDecimals ?: 4,
                    ),
                    size = 8.0f,
                    color = SemanticColor.text_secondary.color.toArgb(),
                    position = AxisTextPosition.OUTSIDE,
                ),
            ),
            rightAxis = AxisConfig(
                drawLine = false,
                drawGrid = false,
                label = null,
            ),
        )
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        selectedPrice.value = e?.data as? MarketCandle
    }

    override fun onNothingSelected() {
        selectedPrice.value = null
    }
}

data class OrderData(
    val price: Double,
    val side: OrderSide,
    val size: Double,
    val formattedPrice: String,
    val orderType: OrderType,
)
