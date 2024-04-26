package exchange.dydx.platformui.components.charts.config

import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

enum class AxisTextPosition {
    INSIDE,
    OUTSIDE,
}

data class LabelConfig(
    val formatter: ValueFormatter,
    val size: Float,
    val color: Int,
    val position: AxisTextPosition = AxisTextPosition.INSIDE,
)

data class AxisConfig(
    val drawLine: Boolean = true,
    val drawGrid: Boolean = false,
    val label: LabelConfig? = null, // null to disable drawing
)

data class DrawingConfig(
    val margin: Float? = null,
    val autoScale: Boolean = true,
    val bgColor: Int? = null,
)

data class InteractionConfig(
    val pan: Boolean = true,
    val doubleTap: Boolean = false,
    val zoom: Boolean = true,
    val highlight: Boolean = true,
    val highlightDistance: Float = 500.0f,
    val selectionListener: OnChartValueSelectedListener? = null,
) {
    val touchEnabled = pan || doubleTap || zoom || highlight

    companion object {
        val default = InteractionConfig()
        val noTouch = InteractionConfig(
            pan = false,
            doubleTap = false,
            zoom = false,
            highlight = false,
        )
    }
}

data class LineChartDrawingConfig(
    val lineWidth: Float = 1.0f,
    val lineColor: Int? = null,
    val fillAlpha: Float? = null, // null to disable fill
    val smooth: Boolean = false,
    val drawCircle: Boolean = false,
    val drawValue: Boolean = false,
)

data class CandlesDrawingConfig(
    val increasingColor: Int? = null,
    val decreasingColor: Int? = null,
    val neutralColor: Int? = null,
)

data class BarDrawingConfig(
    val borderColor: Int? = null,
    val fillColor: Int? = null,
)

interface IChartConfig {
    val drawing: DrawingConfig
    val interaction: InteractionConfig
    val xAxis: AxisConfig?
    val leftAxis: AxisConfig?
    val rightAxis: AxisConfig?
}

interface ILineChartConfig : IChartConfig {
    val lineDrawing: LineChartDrawingConfig
}

interface ICandlesChartConfig : IChartConfig {
    val candlesDrawing: CandlesDrawingConfig
}

interface IBarChartConfig : IChartConfig {
    val barDrawing: BarDrawingConfig
}

interface ICombinedChartConfig : ILineChartConfig, ICandlesChartConfig, IBarChartConfig

data class ChartConfig(
    override val drawing: DrawingConfig,
    override val interaction: InteractionConfig,
    override val xAxis: AxisConfig? = null,
    override val leftAxis: AxisConfig? = null,
    override val rightAxis: AxisConfig? = null,
) : IChartConfig

data class LineChartConfig(
    override val lineDrawing: LineChartDrawingConfig,
    override val drawing: DrawingConfig,
    override val interaction: InteractionConfig,
    override val xAxis: AxisConfig? = null,
    override val leftAxis: AxisConfig? = null,
    override val rightAxis: AxisConfig? = null,
) : ILineChartConfig {
    companion object {
        fun default(): LineChartConfig {
            return LineChartConfig(
                lineDrawing = LineChartDrawingConfig(),
                drawing = DrawingConfig(),
                interaction = InteractionConfig.default,
                xAxis = AxisConfig(),
                leftAxis = null,
                rightAxis = null,
            )
        }
    }
}

data class CombinedChartConfig(
    override val candlesDrawing: CandlesDrawingConfig,
    override val barDrawing: BarDrawingConfig,
    override val lineDrawing: LineChartDrawingConfig,
    override val drawing: DrawingConfig,
    override val interaction: InteractionConfig,
    override val xAxis: AxisConfig? = null,
    override val leftAxis: AxisConfig? = null,
    override val rightAxis: AxisConfig? = null,
) : ICombinedChartConfig {
    companion object {
        fun default(): CombinedChartConfig {
            return CombinedChartConfig(
                candlesDrawing = CandlesDrawingConfig(),
                barDrawing = BarDrawingConfig(),
                lineDrawing = LineChartDrawingConfig(),
                drawing = DrawingConfig(),
                interaction = InteractionConfig.default,
                xAxis = AxisConfig(),
                leftAxis = AxisConfig(),
                rightAxis = AxisConfig(),
            )
        }
    }
}
