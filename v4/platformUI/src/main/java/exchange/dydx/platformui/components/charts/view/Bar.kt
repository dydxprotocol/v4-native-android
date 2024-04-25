package exchange.dydx.platformui.components.charts.view

import android.graphics.Color
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import exchange.dydx.platformui.components.charts.config.BarDrawingConfig
import exchange.dydx.platformui.components.charts.config.DrawingConfig
import exchange.dydx.platformui.components.charts.config.IBarChartConfig
import exchange.dydx.platformui.components.charts.config.InteractionConfig

fun BarChart.config(config: IBarChartConfig) {
    defaultConfig()
    xAxis.config(config.xAxis)
    leftAxis.config(config.leftAxis)
    rightAxis.config(config.rightAxis)
    configDrawing(config.drawing)
    configInteraction(config.interaction)
}

private fun BarChart.configInteraction(interaction: InteractionConfig) {
    setTouchEnabled(interaction.touchEnabled)
    isDragEnabled = interaction.pan
    isDoubleTapToZoomEnabled = interaction.doubleTap
    isScaleXEnabled = interaction.zoom
    isScaleYEnabled = false
    isHighlightPerDragEnabled = interaction.highlight
    setOnChartValueSelectedListener(interaction.selectionListener)
}

private fun BarChart.defaultConfig() {
    legend.enabled = false
    description = null
    setNoDataText("")
    setDrawBorders(false)
    setDrawGridBackground(false)
    setPadding(0, 0, 0, 0)
}

private fun BarChart.configDrawing(drawing: DrawingConfig) {
    isAutoScaleMinMaxEnabled = drawing.autoScale
    setBackgroundColor(drawing.bgColor ?: Color.TRANSPARENT)
    drawing.margin?.let {
        setViewPortOffsets(it, it, it, it)
    }
}

private fun BarChart.apply(
    set: BarDataSet,
    config: IBarChartConfig,
) {
}

fun BarChart.update(set: BarDataSet, config: IBarChartConfig) {
    update(listOf(set), config)
}

fun BarChart.update(
    sets: List<BarDataSet>,
    config: IBarChartConfig,
    updateRange: () -> Unit = {}
) {
    for (i in sets.indices) {
        val set = sets[i]

        set.update(
            config.barDrawing,
            config.drawing,
            config.interaction,
        )
    }

    val hadData = hasData()
    data = BarData(sets)
    if (!hadData) {
        updateRange()
    }

    notifyDataSetChanged()
    invalidate()
}

fun BarChart.hasData(): Boolean {
    data?.let {
        it.dataSets.forEach { set ->
            if (set.entryCount > 0) {
                return true
            }
        }
    }
    return false
}

fun BarDataSet.update(
    barDrawing: BarDrawingConfig,
    drawing: DrawingConfig,
    interaction: InteractionConfig,
) {
    axisDependency = YAxis.AxisDependency.RIGHT
    color = barDrawing.fillColor ?: Color.BLUE
    barBorderWidth = 1.0f
    barBorderColor = barDrawing.borderColor ?: Color.BLUE
    barShadowColor = barDrawing.fillColor ?: Color.BLUE

    label = ""
    setDrawValues(false)
}
