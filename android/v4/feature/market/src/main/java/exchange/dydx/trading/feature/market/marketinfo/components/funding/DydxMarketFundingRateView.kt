package exchange.dydx.trading.feature.market.marketinfo.components.funding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.data.Entry
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.platformui.components.charts.config.LineChartConfig
import exchange.dydx.platformui.components.charts.presenter.LineChartView
import exchange.dydx.platformui.components.charts.view.LineChartDataSet
import exchange.dydx.platformui.components.charts.view.config
import exchange.dydx.platformui.components.charts.view.update
import exchange.dydx.platformui.components.tabgroups.PlatformPillTextGroup
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxMarketFundingRateView() {
    DydxThemedPreviewSurface {
        DydxMarketFundingRateView.Content(Modifier.height(280.dp), DydxMarketFundingRateView.ViewState.preview)
    }
}

object DydxMarketFundingRateView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val config: LineChartConfig,
        val fundingRateData: LineChartDataSet?,
        val resolutionTitles: List<String>?,
        val resolutionIndex: Int? = null,
        val onResolutionChanged: (Int) -> Unit = {},
        val dateTimeText: String? = null,
        val valueText: String? = null,
        val sign: PlatformUISign = PlatformUISign.None,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                LineChartConfig.default(),
                fundingRateData = LineChartDataSet(
                    listOf(
                        Entry(0f, 0f),
                        Entry(2f, 2f),
                        Entry(3f, 3f),
                    ),
                    "funding",
                ),
                resolutionTitles = listOf("1h", "8h", "Annualized"),
                resolutionIndex = 0,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketFundingRateViewModel = hiltViewModel()

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
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            SelectorContent(modifier, state)
            ValueContent(modifier, state)
            ChartContent(modifier, state)
        }
    }

    @Composable
    private fun SelectorContent(modifier: Modifier, state: ViewState) {
        val items = state.resolutionTitles ?: return

        Row(
            modifier = modifier
                .fillMaxWidth(),
        ) {
            PlatformPillTextGroup(
                modifier = Modifier,
                items = items,
                selectedItems = items,
                currentSelection = state.resolutionIndex ?: 0,
                onSelectionChanged = { index ->
                    state.onResolutionChanged(index)
                },
                itemStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                selectedItemStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_primary),
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    @Composable
    private fun ValueContent(modifier: Modifier, state: ViewState) {
        /*
        top: "Current xx Rate" or short higlight datetime
        bottom: Formatted funding rate percentage
         */
        val timeText = state.dateTimeText
        val valueText = state.valueText
        Column(
            modifier = modifier
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = timeText ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.mini)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = valueText ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(
                            when (state.sign) {
                                PlatformUISign.Plus -> ThemeColor.SemanticColor.positiveColor
                                PlatformUISign.Minus -> ThemeColor.SemanticColor.negativeColor
                                PlatformUISign.None -> ThemeColor.SemanticColor.text_tertiary
                            },
                        ),
                )
                Spacer(modifier = Modifier.weight(1f))
            }
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
        regularView.update(
            state.fundingRateData ?: LineChartDataSet(listOf<Entry>(), "Funding"),
            state.config,
            null,
        )

        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            // Embed regular Android View using AndroidView composable
            AndroidView(
                factory = { regularView },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
            )
        }
    }
}
