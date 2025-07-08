package exchange.dydx.platformui.components.charts.presenter

import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

typealias ChartViewDelegate = OnChartValueSelectedListener
typealias LineChartView = LineChart
typealias LineChartData = LineData
typealias CandleStickChartView = CandleStickChart
typealias CandleChartDataSet = CandleDataSet
typealias CandleChartData = CandleData

data class GraphingPresenterConfig<T : ChartData<out IDataSet<out Entry>>?>(
    val xAxisFormatter: ValueFormatter,
    val yAxisFormatter: ValueFormatter,
    val panEnabled: Boolean = true,
    val highlightDistance: Double = 500.0,
    val drawXAxisLine: Boolean = false,
    val drawXAxisText: Boolean = false,
    val drawYAxisText: Boolean = false,
    val outsideXAxisText: Boolean = false,
    val outsideYAxisText: Boolean = false,
    val drawGrid: Boolean = false,
    val lineWidth: Int = 1,
    val chartView: Chart<T>,
)

open class GraphingPresenter<T : ChartData<out IDataSet<out Entry>>?>(
    config: GraphingPresenterConfig<T>
)
