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
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class DydxMarketPricesViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
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
    private val resolutionTitles = listOf(
        localizer.localize("APP.GENERAL.TIME_STRINGS.1MIN"),
        localizer.localize("APP.GENERAL.TIME_STRINGS.5MIN"),
        localizer.localize("APP.GENERAL.TIME_STRINGS.15MIN"),
        localizer.localize("APP.GENERAL.TIME_STRINGS.30MIN"),
        localizer.localize("APP.GENERAL.TIME_STRINGS.1H"),
        localizer.localize("APP.GENERAL.TIME_STRINGS.4H"),
        localizer.localize("APP.GENERAL.TIME_STRINGS.1D"),
    )
    private val resolutionIndex = MutableStateFlow(candlesPeriods.indexOf(abacusStateManager.candlesPeriod.value))
    private val selectedPrice = MutableStateFlow<MarketCandle?>(null)

    val offset = 900

    val anchorDateTime: Instant = run {
        val now = Instant.now()
        now.truncatedTo(ChronoUnit.DAYS)
        now
    }

    val state: Flow<DydxMarketPricesView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput.map { it?.marketId },
            abacusStateManager.state.marketMap,
            abacusStateManager.state.candlesMap,
            selectedPrice,
            typeIndex,
            resolutionIndex,
        ) { marketId, marketMap, candlesMap, selectedPrice, typeIndex, resolutionIndex ->
            if (marketId == null) {
                return@combine null
            }
            val market = marketMap?.get(marketId)
            val allPrices = candlesMap?.get(marketId)
            val candlesPeriod = candlesPeriods[resolutionIndex]
            val prices = allPrices?.candles?.get(candlesPeriod)
            createViewState(
                prices,
                market,
                candlesPeriod,
                selectedPrice,
                typeIndex,
                resolutionIndex,
            )
        }
            .distinctUntilChanged()

    private fun createViewState(
        prices: List<MarketCandle>?,
        market: PerpetualMarket?,
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
                x,
                it.high.toFloat(),
                it.low.toFloat(),
                it.open.toFloat(),
                it.close.toFloat(),
            )
            candleEntry.data = it
            candles.add(candleEntry)

            if (!leftPadded) {
                // Add padding. Otherwise, the first bar is cut off in the middle
                padLeft(volumes, x)
                leftPadded = true
            }
            val barEntry = BarEntry(
                x,
                it.usdVolume.toFloat(),
            )
            barEntry.data = it
            volumes.add(barEntry)

            val lineEntry = Entry(
                x,
                it.close.toFloat(),
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

        return DydxMarketPricesView.ViewState(
            localizer = localizer,
            config = config(market, candlesPeriod),
            market = market?.id,
            CandleDataSet(candles, "candles"),
            BarDataSet(volumes, "volumes"),
            LineChartDataSet(lines, "lines"),
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
            highlight,
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
                increasingColor = ThemeColor.SemanticColor.positiveColor.color.toArgb(),
                decreasingColor = ThemeColor.SemanticColor.negativeColor.color.toArgb(),
                neutralColor = exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.text_primary.color.toArgb(),
            ),
            barDrawing = BarDrawingConfig(
                borderColor = exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.layer_6.color.toArgb(),
                fillColor = exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.layer_6.color.toArgb(),
            ),
            lineDrawing = LineChartDrawingConfig(
                2.0f,
                exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.text_secondary.color.toArgb(),
                null,
            ),
            drawing = DrawingConfig(
                null,
                true,
            ),
            interaction = InteractionConfig.default.copy(
                selectionListener = this,
            ),
            xAxis = AxisConfig(
                true,
                false,
                LabelConfig(
                    DateTimeAxisFormatter(anchorDateTime, candlesPeriod, offset),
                    8.0f,
                    exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.text_secondary.color.toArgb(),
                    AxisTextPosition.OUTSIDE,
                ),
            ),
            leftAxis = AxisConfig(
                false,
                false,
                LabelConfig(
                    PriceAxisFormatter(formatter, market?.configs?.tickSizeDecimals ?: 4),
                    8.0f,
                    exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.text_secondary.color.toArgb(),
                    AxisTextPosition.OUTSIDE,
                ),
            ),
            rightAxis = AxisConfig(
                false,
                false,
                null,
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
