package exchange.dydx.trading.feature.portfolio.components.overview

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Subaccount
import exchange.dydx.abacus.output.SubaccountHistoricalPNL
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.manager.HistoricalPnlPeriod
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.platformui.components.charts.config.AxisConfig
import exchange.dydx.platformui.components.charts.config.DrawingConfig
import exchange.dydx.platformui.components.charts.config.InteractionConfig
import exchange.dydx.platformui.components.charts.config.LineChartConfig
import exchange.dydx.platformui.components.charts.config.LineChartDrawingConfig
import exchange.dydx.platformui.components.charts.view.LineChartDataSet
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.Instant
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class DydxPortfolioChartViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel, OnChartValueSelectedListener {
    private val resolutionTitles = listOf("1d", "7d", "30d", "90d")
    private val resolutionIndex = MutableStateFlow(1)
    private val selectedPnl = MutableStateFlow<SubaccountHistoricalPNL?>(null)

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
            null,
        ),
        interaction = InteractionConfig(
            true,
            false,
            true,
            true,
            500.0f,
            this,
        ),
        xAxis = AxisConfig(false, false, null),
        leftAxis = AxisConfig(false, false, null),
        rightAxis = null,
    )

    val state: Flow<DydxPortfolioChartView.ViewState?> =
        combine(
            abacusStateManager.state.selectedSubaccountPNLs,
            abacusStateManager.state.selectedSubaccount,
            resolutionIndex,
            selectedPnl,
        ) { pnls, subaccount, resolutionIndex, selectedPnl ->
            createViewState(
                pnls,
                subaccount,
                resolutionIndex,
                selectedPnl,
            )
        }
            .distinctUntilChanged()

    private fun createViewState(
        pnls: List<SubaccountHistoricalPNL>?,
        subaccount: Subaccount?,
        resolutionIndex: Int,
        selectedPnl: SubaccountHistoricalPNL?,
    ): DydxPortfolioChartView.ViewState {
        val dataset = pnls?.let {
            val entries = it.map { pnl ->
                val value = pnl.equity.toFloat()
                val time = (pnl.createdAtMilliseconds / 1000).toFloat()
                val entry = Entry(
                    time,
                    value,
                )
                entry.data = pnl
                entry
            }
            LineChartDataSet(
                entries,
                "PNL",
            )
        } ?: LineChartDataSet(listOf(), "PNL")
        val positive = (pnls?.lastOrNull()?.equity ?: 0.0) > (pnls?.firstOrNull()?.equity ?: 0.0)
        val first = pnls?.firstOrNull()?.equity
        val equity = selectedPnl?.equity ?: subaccount?.equity?.current
        val datetimeText = selectedPnl?.createdAtMilliseconds?.let {
            val datetime = Instant.ofEpochMilli(it.toLong())
            formatter.dateTime(datetime)
        }
        return DydxPortfolioChartView.ViewState(
            localizer = localizer,
            config = config,
            chartData = dataset,
            positive,
            resolutionTitles = resolutionTitles,
            resolutionIndex,
            onResolutionChanged = { index ->
                resolutionTitles.getOrNull(index)?.let {
                    this.resolutionIndex.value = index
                    HistoricalPnlPeriod.invoke(it)?.let { period ->
                        abacusStateManager.setHistoricalPNLPeriod(period)
                    }
                }
            },
            dateTimeText = datetimeText,
            valueText = equity?.let {
                formatter.dollar(it, 2)
            },
            periodText = localizer.localizeWithParams(
                path = "APP.GENERAL.PROFIT_AND_LOSS_WITH_DURATION",
                params = mapOf(
                    "PERIOD" to resolutionTitles[resolutionIndex],
                ),
            ),
            diffText = first?.let {
                equity?.let {
                    val diff = equity - first

                    val diffText = formatter.dollar(diff.absoluteValue, 2)

                    val percent = if (first != 0.0) (diff / first) else null
                    val percentText = if (percent != null) formatter.percent(percent.absoluteValue, 2) else null
                    SignedAmountView.ViewState(
                        if (percentText != null) "$diffText ($percentText)" else diffText,
                        PlatformUISign.from(diff),
                        coloringOption = SignedAmountView.ColoringOption.AllText,
                    )
                }
            },
        )
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        val pnl = e?.data as? SubaccountHistoricalPNL
        selectedPnl.value = pnl
    }

    override fun onNothingSelected() {
        selectedPnl.value = null
    }
}
