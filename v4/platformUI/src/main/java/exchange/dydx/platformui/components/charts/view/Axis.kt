package exchange.dydx.platformui.components.charts.view

import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import exchange.dydx.platformui.components.charts.config.AxisConfig
import exchange.dydx.platformui.components.charts.config.AxisTextPosition

fun XAxis.config(config: AxisConfig?) {
    if (config != null) {
        isEnabled = true
        position = XAxis.XAxisPosition.BOTTOM
        setDrawAxisLine(config.drawLine)
        setDrawGridLines(config.drawGrid)
        setDrawLimitLinesBehindData(false)
        setDrawGridLinesBehindData(false)
        config.label?.let {
            setDrawLabels(true)
            setCenterAxisLabels(true)
            textColor = it.color
            textSize = it.size
            labelCount = 5
            valueFormatter = it.formatter
        } ?: setDrawLabels(false)
    } else {
        isEnabled = false
    }
}

fun YAxis.config(config: AxisConfig?, spaceTop: Float? = null, spaceBottom: Float? = null) {
    if (config != null) {
        isEnabled = true
        setDrawAxisLine(config.drawLine)
        setDrawGridLines(config.drawGrid)
        setDrawLimitLinesBehindData(false)
        setDrawGridLinesBehindData(false)
        config.label?.let {
            setDrawLabels(true)
            setCenterAxisLabels(true)
            textColor = it.color
            textSize = it.size
            labelCount = 6
            valueFormatter = it.formatter
            setPosition(
                if (it.position == AxisTextPosition.OUTSIDE) YAxis.YAxisLabelPosition.OUTSIDE_CHART else YAxis.YAxisLabelPosition.INSIDE_CHART,
            )
        } ?: setDrawLabels(false)
        spaceTop?.let {
            setSpaceTop(it * 100)
        }
        spaceBottom?.let {
            setSpaceBottom(it * 100)
        }
    } else {
        isEnabled = false
    }
}
