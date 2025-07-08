package exchange.dydx.platformui.components.charts.view

import android.graphics.Color
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import exchange.dydx.platformui.components.charts.config.DrawingConfig
import exchange.dydx.platformui.components.charts.config.ILineChartConfig
import exchange.dydx.platformui.components.charts.config.InteractionConfig
import exchange.dydx.platformui.components.charts.config.LineChartDrawingConfig
import exchange.dydx.platformui.components.charts.presenter.LineChartData
import exchange.dydx.platformui.components.charts.presenter.LineChartView
import exchange.dydx.platformui.components.charts.touch.LineChartTouchHandler
import exchange.dydx.platformui.components.charts.touch.LongPressTouchListener

fun LineChartView.config(config: ILineChartConfig) {
    defaultConfig()
    xAxis.config(config.xAxis)
    leftAxis.config(config.leftAxis)
    rightAxis.config(config.rightAxis)
    configDrawing(config.drawing)
    configInteraction(config.interaction)
}

private fun LineChart.configInteraction(interaction: InteractionConfig) {
    setTouchEnabled(interaction.touchEnabled)
    isDragEnabled = interaction.pan
    isDoubleTapToZoomEnabled = interaction.doubleTap
    isScaleXEnabled = interaction.zoom
    isScaleYEnabled = true
    isHighlightPerDragEnabled = interaction.highlight
    setOnChartValueSelectedListener(interaction.selectionListener)
    if (isHighlightPerDragEnabled) {
        onTouchListener = LongPressTouchListener(this, viewPortHandler.matrixTouch, 3f)
        onChartGestureListener =
            LineChartTouchHandler(this as BarLineChartBase<BarLineScatterCandleBubbleData<IBarLineScatterCandleBubbleDataSet<Entry>>>)
    }
}

private fun LineChartView.defaultConfig() {
    legend.enabled = false
    description = null
    setNoDataText("")
    setDrawBorders(false)
    setDrawGridBackground(false)
    setPadding(0, 0, 0, 0)
}

private fun LineChartView.configDrawing(drawing: DrawingConfig) {
    isAutoScaleMinMaxEnabled = drawing.autoScale
    setBackgroundColor(drawing.bgColor ?: Color.TRANSPARENT)
    drawing.margin?.let {
        setViewPortOffsets(it, it, it, it)
    }
}

private fun LineChartView.apply(
    set: LineChartDataSet,
    config: ILineChartConfig,
    lineColor: Int? = null
) {
}

fun LineChartView.update(set: LineChartDataSet, config: ILineChartConfig, lineColor: Int? = null) {
    val lineColors = if (lineColor != null) listOf(lineColor) else null
    update(listOf(set), config, lineColors)
}

fun LineChartView.update(
    sets: List<LineChartDataSet>,
    config: ILineChartConfig,
    lineColors: List<Int>? = null,
    updateRange: (hadData: Boolean) -> Unit = {}
) {
    for (i in sets.indices) {
        val set = sets[i]
        val lineColor = lineColors?.getOrNull(i)

        set.update(
            lineDrawing = config.lineDrawing,
            drawing = config.drawing,
            interaction = config.interaction,
            lineColor = lineColor,
        )
    }

    data = LineChartData(sets)

    val hadData = hasData()
    this.data = data
    updateRange(hadData)

    notifyDataSetChanged()
    invalidate()
}

fun LineChartView.hasData(): Boolean {
    data?.let {
        it.dataSets.forEach { set ->
            if (set.entryCount > 0) {
                return true
            }
        }
    }
    return false
}

fun LineDataSet.update(
    lineDrawing: LineChartDrawingConfig,
    drawing: DrawingConfig,
    interaction: InteractionConfig,
    lineColor: Int? = null,
) {
    label = ""
    axisDependency = YAxis.AxisDependency.LEFT
    lineWidth = lineDrawing.lineWidth
    color = lineColor ?: lineDrawing.lineColor ?: Color.BLUE

    lineDrawing.fillAlpha?.let {
        drawFilledEnabled = true
        fillColor = color
        fillAlpha = (255.0f * it).toInt()
    } ?: run {
        drawFilledEnabled = false
    }

    drawCirclesEnabled = lineDrawing.drawCircle
    drawValuesEnabled = lineDrawing.drawValue
    circleRadius = 4.0f
    circleHoleRadius = 2.0f
    mode = if (lineDrawing.smooth) LineDataSet.Mode.HORIZONTAL_BEZIER else LineDataSet.Mode.LINEAR
    setDrawVerticalHighlightIndicator(interaction.highlight)
    setDrawHorizontalHighlightIndicator(false)
}
