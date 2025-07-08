package exchange.dydx.platformui.components.charts.view

import android.graphics.Color
import android.graphics.Paint
import com.github.mikephil.charting.components.YAxis
import exchange.dydx.platformui.components.charts.config.CandlesDrawingConfig
import exchange.dydx.platformui.components.charts.config.DrawingConfig
import exchange.dydx.platformui.components.charts.config.IBarChartConfig
import exchange.dydx.platformui.components.charts.config.ICandlesChartConfig
import exchange.dydx.platformui.components.charts.config.InteractionConfig
import exchange.dydx.platformui.components.charts.presenter.CandleChartData
import exchange.dydx.platformui.components.charts.presenter.CandleChartDataSet
import exchange.dydx.platformui.components.charts.presenter.CandleStickChartView

fun CandleStickChartView.config(config: IBarChartConfig) {
    defaultConfig()
    xAxis.config(config.xAxis)
    leftAxis.config(config.leftAxis)
    rightAxis.config(config.rightAxis)
    configDrawing(config.drawing)
    configInteraction(config.interaction)
}

private fun CandleStickChartView.configInteraction(interaction: InteractionConfig) {
    setTouchEnabled(interaction.touchEnabled)
    isDragEnabled = interaction.pan
    isDoubleTapToZoomEnabled = interaction.doubleTap
    isScaleXEnabled = interaction.zoom
    isScaleYEnabled = false
    isHighlightPerDragEnabled = interaction.highlight
    setOnChartValueSelectedListener(interaction.selectionListener)
}

private fun CandleStickChartView.defaultConfig() {
    legend.enabled = false
    description = null
    setNoDataText("")
    setDrawBorders(false)
    setDrawGridBackground(false)
    setPadding(0, 0, 0, 0)
}

private fun CandleStickChartView.configDrawing(drawing: DrawingConfig) {
    isAutoScaleMinMaxEnabled = drawing.autoScale
    setBackgroundColor(drawing.bgColor ?: Color.TRANSPARENT)
    drawing.margin?.let {
        setViewPortOffsets(it, it, it, it)
    }
}

private fun CandleStickChartView.apply(
    set: CandleChartDataSet,
    config: ICandlesChartConfig,
) {
}

fun CandleStickChartView.update(set: CandleChartDataSet, config: ICandlesChartConfig) {
    update(listOf(set), config)
}

fun CandleStickChartView.update(
    sets: List<CandleChartDataSet>,
    config: ICandlesChartConfig,
    updateRange: () -> Unit = {}
) {
    for (i in sets.indices) {
        val set = sets[i]

        set.update(
            config.candlesDrawing,
            config.drawing,
            config.interaction,
        )
    }

    val hadData = hasData()
    data = CandleChartData(sets)
    if (!hadData) {
        updateRange()
    }

    notifyDataSetChanged()
    postInvalidate()
}

fun CandleStickChartView.hasData(): Boolean {
    data?.let {
        it.dataSets.forEach { set ->
            if (set.entryCount > 0) {
                return true
            }
        }
    }
    return false
}

fun CandleChartDataSet.update(
    candlesDrawing: CandlesDrawingConfig,
    drawing: DrawingConfig,
    interaction: InteractionConfig,
    lineColor: Int? = null,
) {
    label = ""
    drawValuesEnabled = false
    drawIconsEnabled = false
    axisDependency = YAxis.AxisDependency.LEFT
    increasingColor = candlesDrawing.increasingColor ?: Color.GREEN
    decreasingColor = candlesDrawing.decreasingColor ?: Color.RED
    increasingPaintStyle = Paint.Style.FILL_AND_STROKE
    decreasingPaintStyle = Paint.Style.FILL_AND_STROKE
    showCandleBar = true
    shadowColorSameAsCandle = true
    shadowWidth = 1.0f

    setDrawVerticalHighlightIndicator(interaction.highlight)
    setDrawHorizontalHighlightIndicator(false)
}
