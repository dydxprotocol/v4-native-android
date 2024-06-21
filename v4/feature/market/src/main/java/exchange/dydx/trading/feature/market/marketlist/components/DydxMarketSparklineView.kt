package exchange.dydx.trading.feature.market.marketlist.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.data.Entry
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.platformui.components.charts.config.AxisConfig
import exchange.dydx.platformui.components.charts.config.DrawingConfig
import exchange.dydx.platformui.components.charts.config.InteractionConfig
import exchange.dydx.platformui.components.charts.config.LineChartConfig
import exchange.dydx.platformui.components.charts.config.LineChartDrawingConfig
import exchange.dydx.platformui.components.charts.presenter.LineChartView
import exchange.dydx.platformui.components.charts.view.LineChartDataSet
import exchange.dydx.platformui.components.charts.view.config
import exchange.dydx.platformui.components.charts.view.update
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketViewState

@Preview
@Composable
fun Preview_DydxMarketSparklineView() {
    DydxThemedPreviewSurface {
        DydxMarketSparklineView.Content(Modifier, DydxMarketSparklineView.ViewState.preview)
    }
}

object DydxMarketSparklineView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val sharedMarketViewState: SharedMarketViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                sharedMarketViewState = SharedMarketViewState.preview,
            )
        }
    }

    private val config: LineChartConfig = LineChartConfig(
        lineDrawing = LineChartDrawingConfig(
            2.0f,
            ThemeColor.SemanticColor.text_primary.color.toArgb(),
            null,
            true,
        ),
        drawing = DrawingConfig(
            0.0f,
            true,
        ),
        interaction = InteractionConfig.noTouch,
        xAxis = AxisConfig(false, false),
        leftAxis = AxisConfig(false, false),
        rightAxis = null,
    )

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketSparklineViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        val context = LocalContext.current
        // Create a reference to the regular Android View
        val regularView = remember {
            LineChartView(context).apply {
                config(config)
            }
        }
        regularView.update(
            set = state.sharedMarketViewState?.sparkline ?: LineChartDataSet(
                listOf<Entry>(),
                "Sparkline",
            ),
            config = config,
            lineColor = state.sharedMarketViewState?.priceChangePercent24H?.sign?.let {
                when (it) {
                    PlatformUISign.Plus -> ThemeColor.SemanticColor.positiveColor.color.toArgb()
                    PlatformUISign.Minus -> ThemeColor.SemanticColor.negativeColor.color.toArgb()
                    else -> ThemeColor.SemanticColor.text_primary.color.toArgb()
                }
            }
                ?: exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.text_secondary.color.toArgb(),
        )

        Column(
            modifier = modifier
                .width(48.dp)
                .padding(vertical = 0.dp)
                .padding(horizontal = 0.dp),
        ) {
            // Embed regular Android View using AndroidView composable
            AndroidView(
                factory = { regularView },
            )
        }
    }
}
