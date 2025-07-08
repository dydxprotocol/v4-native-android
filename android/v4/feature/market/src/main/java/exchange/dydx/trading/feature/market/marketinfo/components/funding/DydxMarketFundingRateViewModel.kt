package exchange.dydx.trading.feature.market.marketinfo.components.funding

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.MarketHistoricalFunding
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.platformui.components.charts.config.AxisConfig
import exchange.dydx.platformui.components.charts.config.AxisTextPosition
import exchange.dydx.platformui.components.charts.config.DrawingConfig
import exchange.dydx.platformui.components.charts.config.InteractionConfig
import exchange.dydx.platformui.components.charts.config.LabelConfig
import exchange.dydx.platformui.components.charts.config.LineChartConfig
import exchange.dydx.platformui.components.charts.config.LineChartDrawingConfig
import exchange.dydx.platformui.components.charts.formatter.DateTimeResolution
import exchange.dydx.platformui.components.charts.formatter.TimeAxisFormatter
import exchange.dydx.platformui.components.charts.view.LineChartDataSet
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DydxMarketFundingRateViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel, OnChartValueSelectedListener {
    private val resolutionTitles = listOf("1h", "8h", "Annualized")
    private val resolutionIndex = MutableStateFlow(0)
    private val selectedFunding = MutableStateFlow<MarketHistoricalFunding?>(null)

    private val config: LineChartConfig = LineChartConfig(
        lineDrawing = LineChartDrawingConfig(
            2.0f,
            exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.text_primary.color.toArgb(),
            null,
            true,
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
                TimeAxisFormatter(DateTimeResolution.DATE),
                8.0f,
                exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.text_secondary.color.toArgb(),
                AxisTextPosition.INSIDE,
            ),
        ),
        leftAxis = AxisConfig(
            false,
            false,
            LabelConfig(
                PercentAxisFormatter(formatter, 4),
                8.0f,
                exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.text_secondary.color.toArgb(),
                AxisTextPosition.OUTSIDE,
            ),
        ),
        rightAxis = null,
    )

    val state: Flow<DydxMarketFundingRateView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput.map { it?.marketId },
            abacusStateManager.state.marketMap,
            abacusStateManager.state.historicalFundingsMap,
            selectedFunding,
            resolutionIndex,
        ) { marketId, marketMap, historicalFundingsMap, selectedFunding, resolutionIndex ->
            if (marketId == null) {
                return@combine null
            }
            val funding = historicalFundingsMap?.get(marketId)
            createViewState(
                funding,
                marketMap?.get(marketId)?.perpetual?.nextFundingRate,
                selectedFunding,
                resolutionIndex,
            )
        }
            .distinctUntilChanged()

    private fun createViewState(
        funding: List<MarketHistoricalFunding>?,
        fundingRate: Double?,
        selectedFunding: MarketHistoricalFunding?,
        resolutionIndex: Int = 0,
    ): DydxMarketFundingRateView.ViewState {
        val fundingRateData = funding?.let {
            val multiplier = when (resolutionIndex) {
                0 -> 1
                1 -> 8
                else -> 24 * 365
            }
            LineChartDataSet(
                it.map { fundingLine ->
                    val entry = Entry(
                        fundingLine.effectiveAtMilliseconds.toFloat(),
                        fundingLine.rate.toFloat() * multiplier,
                    )
                    entry.data = fundingLine
                    entry
                },
                "Funding",
            )
        }
        val datetimeText = selectedFunding?.effectiveAtMilliseconds?.let {
            val datetime = Instant.ofEpochMilli(it.toLong())
            formatter.dateTime(datetime)
        } ?: when (resolutionIndex) {
            0 -> localizer.localize("APP.TRADE.CURRENT_RATE_1H")
            1 -> localizer.localize("APP.TRADE.CURRENT_RATE_8H")
            else -> localizer.localize("APP.TRADE.CURRENT_ANNUALIZED_RATE")
        }

        val fundingRateText = (selectedFunding?.rate ?: fundingRate)?.let {
            when (resolutionIndex) {
                0 -> formatter.percent(it, 6)
                1 -> formatter.percent(it * 8, 4)
                else -> formatter.percent(it * 24 * 365, 2)
            }
        }
        val sign = (selectedFunding?.rate ?: fundingRate)?.let {
            when {
                it > 0.0 -> PlatformUISign.Plus
                it < 0.0 -> PlatformUISign.Minus
                else -> PlatformUISign.None
            }
        } ?: PlatformUISign.None

        return DydxMarketFundingRateView.ViewState(
            localizer = localizer,
            config = config,
            fundingRateData = fundingRateData,
            resolutionTitles,
            resolutionIndex,
            onResolutionChanged = { this.resolutionIndex.value = it },
            datetimeText,
            fundingRateText,
            sign,
        )
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        selectedFunding.value = e?.data as? MarketHistoricalFunding
    }

    override fun onNothingSelected() {
        selectedFunding.value = null
    }
}
