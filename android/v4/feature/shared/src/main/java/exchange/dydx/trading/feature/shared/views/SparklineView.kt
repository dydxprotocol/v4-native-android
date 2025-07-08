package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.data.Entry
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.platformui.components.charts.config.AxisConfig
import exchange.dydx.platformui.components.charts.config.DrawingConfig
import exchange.dydx.platformui.components.charts.config.InteractionConfig
import exchange.dydx.platformui.components.charts.config.LineChartConfig
import exchange.dydx.platformui.components.charts.config.LineChartDrawingConfig
import exchange.dydx.platformui.components.charts.presenter.LineChartView
import exchange.dydx.platformui.components.charts.view.LineChartDataSet
import exchange.dydx.platformui.components.charts.view.config
import exchange.dydx.platformui.components.charts.view.firstOrNull
import exchange.dydx.platformui.components.charts.view.lastOrNull
import exchange.dydx.platformui.components.charts.view.update
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface

@Preview
@Composable
fun Preview_SparklineView() {
    DydxThemedPreviewSurface {
        SparklineView.Content(Modifier, SparklineView.ViewState.preview)
    }
}

object SparklineView {
    data class ViewState(
        val sparkline: LineChartDataSet? = null,
        val sign: PlatformUISign? = null,
        val lineChartConfig: LineChartConfig? = null,
    ) {
        companion object {
            val preview = ViewState(
                sparkline = LineChartDataSet(
                    listOf(
                        Entry(0f, 0f),
                        Entry(1f, 1f),
                        Entry(2f, 2f),
                        Entry(3f, 3f),
                        Entry(4f, 4f),
                    ),
                    "Sparkline",
                ),
                sign = PlatformUISign.Plus,
            )

            val defaultLineChartConfig: LineChartConfig
                get() = LineChartConfig(
                    lineDrawing = LineChartDrawingConfig(
                        lineWidth = 2.0.toFloat(),
                        lineColor = ThemeColor.SemanticColor.text_primary.color.toArgb(),
                        fillAlpha = null,
                        smooth = true,
                    ),
                    drawing = DrawingConfig(
                        margin = 0.0f,
                        autoScale = true,
                    ),
                    interaction = InteractionConfig.default,
                    xAxis = AxisConfig(drawLine = false, drawGrid = false),
                    leftAxis = AxisConfig(drawLine = false, drawGrid = false),
                )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        val config = state.lineChartConfig ?: ViewState.defaultLineChartConfig

        val context = LocalContext.current
        // Create a reference to the regular Android View
        val regularView = remember {
            LineChartView(context).apply {
                config(config)
            }
        }
        val sign = state.sign ?: run {
            val first = state.sparkline?.firstOrNull()?.y ?: 0f
            val last = state.sparkline?.lastOrNull()?.y ?: 0f
            when {
                first < last -> PlatformUISign.Plus
                first > last -> PlatformUISign.Minus
                else -> PlatformUISign.None
            }
        }
        regularView.update(
            set = state.sparkline ?: LineChartDataSet(
                listOf<Entry>(),
                "Sparkline",
            ),
            config = config,
            lineColor = when (sign) {
                PlatformUISign.Plus -> ThemeColor.SemanticColor.positiveColor.color.toArgb()
                PlatformUISign.Minus -> ThemeColor.SemanticColor.negativeColor.color.toArgb()
                PlatformUISign.None -> ThemeColor.SemanticColor.text_tertiary.color.toArgb()
            },
        )
        // Embed regular Android View using AndroidView composable
        AndroidView(
            factory = { regularView },
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
        )
    }
}
