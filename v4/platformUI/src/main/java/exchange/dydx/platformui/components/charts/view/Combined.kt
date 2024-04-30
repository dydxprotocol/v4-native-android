package exchange.dydx.platformui.components.charts.view

import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.google.android.gms.common.data.DataBufferUtils.hasData
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

fun CombinedChart.config(config: ICombinedChartConfig) {
    defaultConfig()
    xAxis.config(config.xAxis)
    leftAxis.config(config.leftAxis, null, 0.2f)
    rightAxis.config(config.rightAxis, 0.85f, 0.0f)
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
    limits: List<LimitLine>,
    config: ICombinedChartConfig,
    lineColor: Int? = null,
    updateRange: (lastX: Float) -> Unit = {}
) {
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

    val hadData = hasData()
    this.data = data
    if (!hadData) {
        bars?.values?.lastOrNull()?.x?.let {
            updateRange(it)
        }
    }

    limits.forEach {
        this.axisLeft.addLimitLine(it)
    }

    notifyDataSetChanged()
    invalidate()
}

fun CombinedChart.hasData(): Boolean {
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
