package exchange.dydx.trading.feature.portfolio.components.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.data.Entry
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.charts.config.LineChartConfig
import exchange.dydx.platformui.components.charts.presenter.LineChartView
import exchange.dydx.platformui.components.charts.view.LineChartDataSet
import exchange.dydx.platformui.components.charts.view.config
import exchange.dydx.platformui.components.charts.view.update
import exchange.dydx.platformui.components.tabgroups.PlatformTextTabGroup
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.feature.shared.views.SignedAmountView

@Preview
@Composable
fun Preview_DydxPortfolioChartView() {
    DydxThemedPreviewSurface {
        DydxPortfolioChartView.Content(Modifier, DydxPortfolioChartView.ViewState.preview)
    }
}

object DydxPortfolioChartView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val config: LineChartConfig,
        val chartData: LineChartDataSet,
        val positive: Boolean,
        val resolutionTitles: List<String>?,
        val resolutionIndex: Int? = null,
        val onResolutionChanged: (Int) -> Unit = {},
        val dateTimeText: String? = null,
        val valueText: String? = null,
        val periodText: String? = null,
        val diffText: SignedAmountView.ViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                config = LineChartConfig.default(),
                chartData = LineChartDataSet(
                    listOf(
                        Entry(0f, 0f),
                        Entry(2f, 2f),
                        Entry(3f, 3f),
                    ),
                    "test",
                ),
                true,
                listOf("1D", "1W", "1M", "3M", "6M", "1Y", "ALL"),
                0,
                {},
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxPortfolioChartViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = ThemeShapes.VerticalPadding)
                .padding(horizontal = ThemeShapes.HorizontalPadding)
                .background(
                    color = ThemeColor.SemanticColor.layer_3.color,
                    shape = RoundedCornerShape(16.dp),
                ),

        ) {
            ValueContent(Modifier, state)
            ChartContent(Modifier, state)
            SelectorContent(Modifier, state)
        }
    }

    @Composable
    private fun ChartContent(modifier: Modifier, state: ViewState) {
        val context = LocalContext.current
        // Create a reference to the regular Android View
        val regularView = remember {
            LineChartView(context).apply {
                config(state.config)
            }
        }
        val color = if (state.positive) {
            ThemeColor.SemanticColor.positiveColor.color
        } else {
            ThemeColor.SemanticColor.negativeColor.color
        }
        regularView.update(state.chartData, state.config, color.toArgb())

        Column {
            // Embed regular Android View using AndroidView composable
            AndroidView(
                factory = { regularView },
                modifier = modifier
                    .fillMaxWidth()
                    .height(180.dp),
            )
        }
    }

    @Composable
    private fun ValueContent(modifier: Modifier, state: ViewState?) {
        /*
        topleft: "Portfolio Value" or short higlight datetime
        topRight: "{time period} P&L"
        bottomLeft: Dollar amount
        bottomRight: P&L amount and percentage
         */
        val timeText = state?.dateTimeText ?: state?.localizer?.localize("APP.PORTFOLIO.PORTFOLIO_VALUE")
        Column(
            modifier = modifier
                .padding(horizontal = 32.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(
                    text = timeText ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = state?.periodText ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(
                    text = state?.valueText ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.large)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                Spacer(modifier = Modifier.weight(1f))

                SignedAmountView.Content(
                    modifier = Modifier,
                    state = state?.diffText,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                )
            }
        }
    }

    @Composable
    private fun SelectorContent(modifier: Modifier, state: ViewState?) {
        val items = state?.resolutionTitles ?: return

        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.weight(1f))

                PlatformTextTabGroup(
                    modifier = modifier,
                    items = items,
                    selectedItems = items,
                    currentSelection = state.resolutionIndex ?: 0,
                    onSelectionChanged = { index ->
                        state.onResolutionChanged(index)
                    },
                    itemStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    selectedItemStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
