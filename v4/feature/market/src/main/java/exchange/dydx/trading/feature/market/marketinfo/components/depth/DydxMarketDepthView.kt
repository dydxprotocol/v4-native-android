package exchange.dydx.trading.feature.market.marketinfo.components.depth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.data.Entry
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.charts.config.LineChartConfig
import exchange.dydx.platformui.components.charts.presenter.LineChartView
import exchange.dydx.platformui.components.charts.view.LineChartDataSet
import exchange.dydx.platformui.components.charts.view.config
import exchange.dydx.platformui.components.charts.view.update
import exchange.dydx.platformui.components.textgroups.PlatformHorizontalTextGroup
import exchange.dydx.platformui.components.textgroups.TextPair
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxMarketDepthView() {
    DydxThemedPreviewSurface {
        DydxMarketDepthView.Content(Modifier, DydxMarketDepthView.ViewState.preview)
    }
}

object DydxMarketDepthView : DydxComponent {
    private var hasCentered = false
    private var market: String? = null
    data class ViewState(
        val localizer: LocalizerProtocol,
        val config: LineChartConfig,
        val bidsData: LineChartDataSet?,
        val asksData: LineChartDataSet?,
        val market: String?,
        val midPrice: Float?,
        val lastBidWithin2pct: Float?,
        val lastAskWithin2pct: Float?,
        val selectedTextPairs: List<TextPair>? = null,
    ) {
        companion object {
            val bids = listOf(
                Entry(0f, 0f),
                Entry(2f, 2f),
                Entry(3f, 3f),
            )
            val asks = listOf(
                Entry(0f, 0f),
                Entry(2f, 2f),
                Entry(3f, 3f),
            )
            val preview = ViewState(
                localizer = MockLocalizer(),
                LineChartConfig.default(),
                bidsData = LineChartDataSet(
                    bids,
                    "bids",
                ),
                asksData = LineChartDataSet(
                    asks,
                    "asks",
                ),
                market = "BTC-USD",
                midPrice = 1f,
                lastBidWithin2pct = null,
                lastAskWithin2pct = null,
            )
        }
    }

    private fun center(chart: LineChartView, center: Float?, min: Float?, max: Float?) {
        if (min != null) {
            if (max != null) {
                val xCenter = center ?: ((min + max) / 2f)
                val span = (max - min)
                val centerOffsetBySpread = xCenter - span
                chart.setVisibleXRange(span * 2, span * 2)
                chart.moveViewToX(centerOffsetBySpread)
                chart.setVisibleXRange(span * 8, span * 2)
                hasCentered = true
            }
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketDepthViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            ChartContent(modifier, state)
        }

        state.selectedTextPairs?.let {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                ValueContent(modifier, it)
            }
        }
    }

    @Composable
    fun ValueContent(modifier: Modifier, textPairs: List<TextPair>) {
        Column(
            modifier = modifier
                .offset(x = 20.dp, y = 20.dp)
                .width(200.dp)
                .padding(vertical = ThemeShapes.VerticalPadding)
                .padding(horizontal = ThemeShapes.HorizontalPadding)
                .background(
                    color = exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.layer_3.color,
                    shape = RoundedCornerShape(8.dp),
                ),
        ) {
            textPairs.forEach {
                Row {
                    PlatformHorizontalTextGroup(modifier, it)
                }
            }
        }
    }

    @Composable
    fun ChartContent(modifier: Modifier, state: ViewState) {
        val context = LocalContext.current
        // Create a reference to the regular Android View
        val chart = remember {
            LineChartView(context).apply {
                config(state.config)
            }
        }
        val asksColor = ThemeColor.SemanticColor.positiveColor.color
        val bidsColor = ThemeColor.SemanticColor.negativeColor.color

        val data = mutableListOf<LineChartDataSet>()
        val colors = mutableListOf<Int>()
        state.bidsData?.let {
            data.add(it)
            colors.add(bidsColor.toArgb())
        }
        state.asksData?.let {
            data.add(it)
            colors.add(asksColor.toArgb())
        }
        chart.update(
            data,
            state.config,
            colors,
        ) {
            if (market != state.market) {
                center(chart, state.midPrice, state.lastBidWithin2pct, state.lastAskWithin2pct)
                market = state.market
            }
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            // Embed regular Android View using AndroidView composable
            AndroidView(
                factory = { chart },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
            )
        }
    }
}
