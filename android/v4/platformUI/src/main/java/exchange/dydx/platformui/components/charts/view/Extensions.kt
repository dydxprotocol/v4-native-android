package exchange.dydx.platformui.components.charts.view

import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import exchange.dydx.platformui.components.charts.presenter.CandleChartDataSet
import exchange.dydx.platformui.components.charts.presenter.ChartViewDelegate

var <T : BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry>>?> BarLineChartBase<T>.pinchZoomEnabled: Boolean
    get() = isPinchZoomEnabled
    set(value) = setPinchZoom(value)
val <T : BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry>>?> BarLineChartBase<T>.leftAxis: YAxis
    get() = axisLeft
val <T : BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry>>?> BarLineChartBase<T>.rightAxis: YAxis
    get() = axisRight
val <T : BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry>>?> BarLineChartBase<T>.chartDescription: Description
    get() = description

// var <T : BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry>>?> BarLineChartBase<T>.maxVisibleCount: Int
//    get() = 0
//    set(value) = setMaxVisibleValueCount(value)
var <T : BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry>>?> BarLineChartBase<T>.drawBordersEnabled: Boolean
    get() = isDrawBordersEnabled
    set(value) = setDrawBorders(value)
var <T : BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry>>?> BarLineChartBase<T>.dragEnabled: Boolean
    get() = isDragEnabled
    set(value) {
        isDragEnabled = value
    }
var <T : BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry>>?> BarLineChartBase<T>.delegate: ChartViewDelegate?
    get() = null
    set(value) {
        setOnChartValueSelectedListener(value)
    }

var CandleStickChart.maxVisible: Int
    get() = getMaxVisibleCount()
    set(value) = setMaxVisibleValueCount(value)

var AxisBase.enabled: Boolean
    get() = isEnabled
    set(value) {
        isEnabled = value
    }
var AxisBase.drawAxisLineEnabled: Boolean
    get() = isDrawAxisLineEnabled
    set(value) = setDrawAxisLine(value)
var XAxis.labelPosition: XAxis.XAxisPosition
    get() = position
    set(value) {
        position = value
    }

typealias LineChartDataSet = LineDataSet

fun LineDataSet.lastOrNull(): Entry? {
    return if (entryCount > 0) {
        getEntryForIndex(entryCount - 1)
    } else {
        null
    }
}

fun LineDataSet.firstOrNull(): Entry? {
    return if (entryCount > 0) {
        getEntryForIndex(0)
    } else {
        null
    }
}

var LineDataSet.drawCirclesEnabled: Boolean
    get() = isDrawCirclesEnabled
    set(value) = setDrawCircles(value)
var LineDataSet.drawValuesEnabled: Boolean
    get() = isDrawValuesEnabled
    set(value) = setDrawValues(value)
var LineDataSet.drawFilledEnabled: Boolean
    get() = isDrawFilledEnabled
    set(value) = setDrawFilled(value)

var CandleChartDataSet.drawIconsEnabled: Boolean
    get() = isDrawIconsEnabled
    set(value) = setDrawIcons(value)
var CandleChartDataSet.drawValuesEnabled: Boolean
    get() = isDrawValuesEnabled
    set(value) = setDrawValues(value)

var Description.enabled: Boolean
    get() = isEnabled
    set(value) {
        isEnabled = value
    }

var Legend.drawInside: Boolean
    get() = isDrawInsideEnabled
    set(value) = setDrawInside(value)
var Legend.enabled: Boolean
    get() = isEnabled
    set(value) {
        isEnabled = value
    }
