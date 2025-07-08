package exchange.dydx.trading.feature.portfolio.components.overview

import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.output.account.SubaccountHistoricalPNL
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.manager.HistoricalPnlPeriod
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.platformui.components.charts.config.InteractionConfig
import exchange.dydx.platformui.components.charts.view.LineChartDataSet
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.shared.views.SparklineView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.Instant
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.math.sign

@HiltViewModel
class DydxPortfolioChartViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel, OnChartValueSelectedListener {
    private val resolutionTitles = listOf("1d", "7d", "30d", "90d")
    private val resolutionIndex = MutableStateFlow(1)
    private val selectedPnl = MutableStateFlow<SubaccountHistoricalPNL?>(null)

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
            var entries = it.map { pnl ->
                val value = pnl.equity.toFloat()
                val time = (pnl.createdAtMilliseconds / 1000).toFloat()
                val entry = Entry(
                    /* x = */ time,
                    /* y = */ value,
                )
                entry.data = pnl
                entry
            }
            val currentValue = subaccount?.equity?.current?.toFloat()
            if (currentValue != null) {
                entries = entries.toMutableList().apply {
                    add(
                        Entry(
                            /* x = */ (System.currentTimeMillis() / 1000).toFloat(),
                            /* y = */ currentValue,
                        ),
                    )
                }
            }
            LineChartDataSet(
                entries,
                "PNL",
            )
        } ?: LineChartDataSet(listOf(), "PNL")
        val positive = (pnls?.lastOrNull()?.equity ?: 0.0) > (pnls?.firstOrNull()?.equity ?: 0.0)
        val firstEquity = pnls?.firstOrNull()?.equity
        val lastEquity = selectedPnl?.equity ?: subaccount?.equity?.current ?: pnls?.lastOrNull()?.equity
        val equity = selectedPnl?.equity ?: subaccount?.equity?.current
        val datetimeText = selectedPnl?.createdAtMilliseconds?.let {
            val datetime = Instant.ofEpochMilli(it.toLong())
            formatter.dateTime(datetime)
        }
        return DydxPortfolioChartView.ViewState(
            localizer = localizer,
            sparkline = SparklineView.ViewState(
                sparkline = dataset,
                sign = if (positive) PlatformUISign.Plus else PlatformUISign.Minus,
                lineChartConfig = SparklineView.ViewState.defaultLineChartConfig.copy(
                    lineDrawing = SparklineView.ViewState.defaultLineChartConfig.lineDrawing.copy(
                        lineWidth = 3.0.toFloat(),
                    ),
                    interaction = InteractionConfig.default.copy(
                        selectionListener = this,
                    ),
                ),
            ),
            resolutionTitles = resolutionTitles,
            resolutionIndex = resolutionIndex,
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
            diffText = if (firstEquity != null && lastEquity != null) {
                val diff = lastEquity - firstEquity
                val diffText = formatter.dollar(diff.absoluteValue, 2)

                val percent = if (firstEquity != 0.0) (diff / firstEquity) else null
                val percentText =
                    if (percent != null) formatter.percent(percent.absoluteValue, 2) else null
                SignedAmountView.ViewState(
                    if (percentText != null) "$diffText ($percentText)" else diffText,
                    PlatformUISign.from(diff),
                    coloringOption = SignedAmountView.ColoringOption.TextOnly,
                )
            } else {
                null
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
