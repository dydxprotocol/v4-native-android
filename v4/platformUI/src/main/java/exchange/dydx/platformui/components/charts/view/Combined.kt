package exchange.dydx.platformui.components.charts.view

import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import exchange.dydx.platformui.components.charts.config.DrawingConfig
import exchange.dydx.platformui.components.charts.config.IBarChartConfig
import exchange.dydx.platformui.components.charts.config.ICandlesChartConfig
import exchange.dydx.platformui.components.charts.config.ICombinedChartConfig
import exchange.dydx.platformui.components.charts.config.ILineChartConfig
import exchange.dydx.platformui.components.charts.config.InteractionConfig
import exchange.dydx.platformui.components.charts.presenter.CandleChartData
import exchange.dydx.platformui.components.charts.presenter.CandleChartDataSet
import exchange.dydx.platformui.components.charts.presenter.LineChartData
import exchange.dydx.platformui.components.charts.touch.LineChartTouchHandler
import exchange.dydx.platformui.components.charts.touch.LongPressTouchListener
import kotlin.math.abs

fun CombinedChart.config(config: ICombinedChartConfig) {
    defaultConfig()
    xAxis.config(config.xAxis)
    leftAxis.config(
        config = config.leftAxis,
        spaceTop = null,
        spaceBottom = 0.8f,
    )
    rightAxis.config(
        config = config.rightAxis,
        spaceTop = 0.85f,
        spaceBottom = 0.0f,
    )
    configDrawing(config.drawing)
    configInteraction(config.interaction)
}

private fun CombinedChart.configInteraction(interaction: InteractionConfig) {
    setTouchEnabled(interaction.touchEnabled)
    isDragEnabled = interaction.pan
    isDoubleTapToZoomEnabled = interaction.doubleTap
    isScaleXEnabled = interaction.zoom
    isScaleYEnabled = false
    isHighlightPerDragEnabled = interaction.highlight
    setOnChartValueSelectedListener(interaction.selectionListener)
    if (isHighlightPerDragEnabled) {
        onTouchListener = LongPressTouchListener(this, viewPortHandler.matrixTouch, 3f)
        onChartGestureListener =
            LineChartTouchHandler(this as BarLineChartBase<BarLineScatterCandleBubbleData<IBarLineScatterCandleBubbleDataSet<Entry>>>)
    }
}

private fun CombinedChart.defaultConfig() {
    legend.enabled = false
    description = null
    setNoDataText("")
    setDrawBorders(false)
    setDrawGridBackground(false)
    setPadding(0, 0, 0, 0)
}

private fun CombinedChart.configDrawing(drawing: DrawingConfig) {
    isAutoScaleMinMaxEnabled = drawing.autoScale
    setBackgroundColor(drawing.bgColor ?: android.graphics.Color.TRANSPARENT)
    drawing.margin?.let {
        setViewPortOffsets(it, it, it, it)
    }
}

private fun CombinedChart.apply(
    set: LineChartDataSet,
    config: ILineChartConfig,
    lineColor: Int? = null
) {
    set.update(
        config.lineDrawing,
        config.drawing,
        config.interaction,
        lineColor,
    )
}

private fun CombinedChart.apply(
    set: CandleChartDataSet,
    config: ICandlesChartConfig,
) {
    set.update(
        config.candlesDrawing,
        config.drawing,
        config.interaction,
    )
}

private fun CombinedChart.apply(
    set: BarDataSet,
    config: IBarChartConfig,
) {
    set.update(
        config.barDrawing,
        config.drawing,
        config.interaction,
    )
}

fun CombinedChart.update(
    candles: CandleChartDataSet?,
    bars: BarDataSet?,
    line: LineChartDataSet?,
    config: ICombinedChartConfig,
    lineColor: Int? = null,
    updateRange: (lastX: Float) -> Unit = {}
) {
    config(config)

    candles?.update(
        config.candlesDrawing,
        config.drawing,
        config.interaction,
    )
    bars?.update(
        config.barDrawing,
        config.drawing,
        config.interaction,
    )
    line?.update(
        config.lineDrawing,
        config.drawing,
        config.interaction,
        lineColor,
    )

    this.setPadding(0, 0, 0, 0)

    val data = CombinedData()

    if (candles?.entryCount ?: 0 > 0) {
        data.setData(CandleChartData(listOf(candles)))
    } else {
        data.setData(CandleChartData(listOf()))
    }
    if (bars?.entryCount ?: 0 > 0) {
        data.setData(BarData(listOf(bars)))
    } else {
        data.setData(BarData(listOf()))
    }
    if (line?.entryCount ?: 0 > 0) {
        data.setData(LineChartData(listOf(line)))
    } else {
        data.setData(LineChartData(listOf()))
    }

    // Call updateRange when there is a significant change in data size
    // This is to handle progressive loading of data so that the range
    // is updated only when the data size changes significantly
    val dataSizeChanged = dataSizeChangedOverThreshold(data)
    this.data = data
    if (dataSizeChanged) {
        val lastValue = candles?.values?.lastOrNull()?.x ?: bars?.values?.lastOrNull()?.x ?: line?.values?.lastOrNull()?.x
        if (lastValue != null) {
            updateRange(lastValue)
        }
    }

    notifyDataSetChanged()
    invalidate()
}

private fun CombinedChart.dataSizeChangedOverThreshold(newData: CombinedData): Boolean {
    val diffThreshold = 10 // threshold for entry count difference
    val oldData = data
    if (oldData?.lineData?.dataSets?.size != newData.lineData.dataSets.size ||
        oldData.candleData.dataSets.size != newData.candleData.dataSets.size ||
        oldData.barData.dataSets.size != newData.barData.dataSets.size
    ) {
        return true
    }
    for (i in 0 until newData.lineData.dataSets.size) {
        if (abs(oldData.lineData.dataSets[i].entryCount - newData.lineData.dataSets[i].entryCount) > diffThreshold) {
            return true
        }
    }
    for (i in 0 until newData.candleData.dataSets.size) {
        if (abs(oldData.candleData.dataSets[i].entryCount - newData.candleData.dataSets[i].entryCount) > diffThreshold) {
            return true
        }
    }
    for (i in 0 until newData.barData.dataSets.size) {
        if (abs(oldData.barData.dataSets[i].entryCount - newData.barData.dataSets[i].entryCount) > diffThreshold) {
            return true
        }
    }
    return false
}

private fun CombinedChart.hasData(): Boolean {
    data?.let {
        val lineData = it.lineData
        lineData?.dataSets?.forEach { set ->
            if (set.entryCount > 0) {
                return true
            }
        }
        val candlesData = it.candleData
        candlesData?.dataSets?.forEach { set ->
            if (set.entryCount > 0) {
                return true
            }
        }
        val barData = it.barData
        barData?.dataSets?.forEach { set ->
            if (set.entryCount > 0) {
                return true
            }
        }
    }
    return false
}
