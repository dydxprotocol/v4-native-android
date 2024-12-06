package exchange.dydx.trading.feature.market.marketinfo.components.prices

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformPillItem
import exchange.dydx.platformui.components.charts.config.CombinedChartConfig
import exchange.dydx.platformui.components.charts.presenter.CandleChartDataSet
import exchange.dydx.platformui.components.charts.view.LineChartDataSet
import exchange.dydx.platformui.components.charts.view.config
import exchange.dydx.platformui.components.charts.view.update
import exchange.dydx.platformui.components.menus.PlatformDropdownMenu
import exchange.dydx.platformui.components.menus.PlatformMenuItem
import exchange.dydx.platformui.components.tabgroups.PlatformPillTextGroup
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxMarketPricesView() {
    DydxThemedPreviewSurface {
        DydxMarketPricesView.Content(Modifier.height(280.dp), DydxMarketPricesView.ViewState.preview)
    }
}

data class SelectionOptions(
    val titles: List<String>,
    val index: Int,
    val onChanged: (Int) -> Unit = {},
)

data class PriceHighlight(
    val datetimeText: String,
    val openText: String,
    val highText: String,
    val lowText: String,
    val closeText: String,
    val volumeText: String,
)

object DydxMarketPricesView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val config: CombinedChartConfig,
        val candles: CandleChartDataSet?,
        val volumes: BarDataSet?,
        val prices: LineChartDataSet?,
        val orderLines: List<OrderLineData>,
        val typeOptions: SelectionOptions,
        val resolutionOptions: SelectionOptions,
        val highlight: PriceHighlight? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                config = CombinedChartConfig.default(),
                CandleChartDataSet(
                    listOf(
                        CandleEntry(0f, 0f, 0f, 0f, 0f),
                        CandleEntry(2f, 2f, 2f, 2f, 2f),
                        CandleEntry(3f, 3f, 3f, 3f, 3f),
                    ),
                    "funding",
                ),
                BarDataSet(
                    listOf(
                        BarEntry(0f, 0f),
                        BarEntry(2f, 2f),
                        BarEntry(3f, 3f),
                    ),
                    "funding",
                ),
                LineChartDataSet(
                    listOf(
                        Entry(0f, 0f),
                        Entry(2f, 2f),
                        Entry(3f, 3f),
                    ),
                    "funding",
                ),
                listOf(
                    OrderLineData(1.0, ThemeColor.SemanticColor.color_green.color.toArgb(), ThemeColor.SemanticColor.color_white.color.toArgb(), 1.0, "$1.0", "Limit"),
                ),
                typeOptions = SelectionOptions(
                    titles = listOf("Candles", "Lines"),
                    index = 0,
                ),
                resolutionOptions = SelectionOptions(
                    titles = listOf("1m", "5m", "15m"),
                    index = 0,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketPricesViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle().value
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
            TopContent(modifier = Modifier, state = state)
            ChartContent(modifier = Modifier, state = state)
        }
    }

    @Composable
    private fun TopContent(modifier: Modifier, state: ViewState) {
        Column(
            modifier = modifier
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            if (state.highlight != null) {
                ValueContent(Modifier, state.highlight, state.localizer)
            } else {
                SelectorContent(
                    Modifier,
                    state.typeOptions,
                    state.resolutionOptions,
                )
            }
        }
    }

    @Composable
    private fun ValueContent(modifier: Modifier, highlight: PriceHighlight, localizer: LocalizerProtocol) {
        Column(
            modifier = modifier
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = localizer.localize("APP.GENERAL.VIEW_DATA_FOR"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
                Text(
                    text = highlight.datetimeText,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny)
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "O",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
                Text(
                    text = highlight.openText,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny)
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "H",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
                Text(
                    text = highlight.highText,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny)
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "L",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
                Text(
                    text = highlight.lowText,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny)
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "C",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
                Text(
                    text = highlight.closeText,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny)
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "V",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
                Text(
                    text = highlight.volumeText,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny)
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )
            }
        }
    }

    @Composable
    private fun SelectorContent(
        modifier: Modifier,
        typeOptions: SelectionOptions,
        resolutionOptions: SelectionOptions,
    ) {
        val items = typeOptions.titles

        val expanded: MutableState<Boolean> = remember {
            mutableStateOf(false)
        }

        Row(
            modifier = modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlatformPillTextGroup(
                modifier = Modifier,
                items = items,
                selectedItems = items,
                currentSelection = typeOptions.index,
                onSelectionChanged = { index ->
                    typeOptions.onChanged(index)
                },
                itemStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                selectedItemStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_primary),
            )

            Spacer(modifier = Modifier.weight(1f))

            Box {
                PlatformPillItem(
                    modifier = Modifier
                        .clickable { expanded.value = !expanded.value },
                    backgroundColor = ThemeColor.SemanticColor.layer_5,
                ) {
                    Text(
                        text = resolutionOptions.titles[resolutionOptions.index],
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )
                }

                PlatformDropdownMenu(
                    modifier = Modifier,
                    expanded = expanded,
                    items = resolutionOptions.titles.mapIndexed { index, content ->
                        PlatformMenuItem(
                            text = content,
                            onClick = {
                                resolutionOptions.onChanged(index)
                                expanded.value = false
                            },
                        )
                    },
                    selectedIndex = resolutionOptions.index,
                )
            }
        }
    }

    @Composable
    private fun ChartContent(modifier: Modifier, state: ViewState) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            AndroidView(
                factory = { context ->
                    CombinedChartWithOrderLines(context, state.localizer).apply {
                        config(state.config)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                update = { chart ->
                    chart.orderLines = state.orderLines
                    chart.update(
                        candles = if (state.typeOptions.index == 0) state.candles else null,
                        bars = state.volumes,
                        line = if (state.typeOptions.index == 0) null else state.prices,
                        config = state.config,
                        lineColor = null,
                        updateRange = { lastX ->
                            // Show 40 items
                            chart.setVisibleXRange(40f, 40f)
                            chart.moveViewToX(lastX)
                            //        chart.moveViewToAnimated(lastX, 0f, YAxis.AxisDependency.RIGHT, 500)
                            // The minXRange has a higher number than maxXRange
                            // because the minXRange is the range for minXScale
                            // and the maxXRange is the range for maxXScale
                            // and range and scale are inverse
                            chart.setVisibleXRange(160f, 40f)
                        },
                    )
                },
            )
        }
    }
}

internal class CombinedChartWithOrderLines(
    context: Context,
    private val localizer: LocalizerProtocol,
) : CombinedChart(context) {

    // Compose AndroidView by default does not clip children, let's turn it on.
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as ViewGroup).clipChildren = true
    }

    var orderLines: List<OrderLineData> = emptyList()
        set(value) {
            if (field == value) return
            field = value
            axisLeft.removeAllLimitLines()
            value.forEach { (price, lineColor, textColor) ->
                LimitLine(price.toFloat())
                    .apply {
                        this.lineColor = lineColor
                        this.textColor = textColor
                        enableDashedLine(20f, 10f, 0f)
                    }
                    .also { axisLeft.addLimitLine(it) }
            }
        }
    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        orderLines.forEach { orderLine ->
            // calculate y pixel value for the orderline
            val orderPixelY = run {
                val floatArray = floatArrayOf(0f, orderLine.price.toFloat())
                mLeftAxisTransformer.pointValuesToPixel(floatArray)
                floatArray[1]
            }

            val tagRightX = canvas.drawOrderPriceTag(orderPixelY, orderLine)
            canvas.drawOrderLabelAndSize(tagRightX + 48f, orderPixelY, orderLine)
        }
    }

    private fun Canvas.drawOrderPriceTag(yPos: Float, orderLine: OrderLineData): Float {
        return drawTextView(
            xPos = 0f,
            yPos = yPos,
            text = orderLine.formattedPrice,
            backgroundColor = orderLine.lineColor,
            textColor = orderLine.textColor,
        )
    }

    private fun Canvas.drawOrderLabelAndSize(xPos: Float, yPos: Float, orderLine: OrderLineData) {
        val orderLabelRight = drawOrderLabel(xPos, yPos, orderLine)
        drawOrderSize(orderLabelRight, yPos, orderLine)
    }

    private fun Canvas.drawOrderLabel(xPos: Float, yPos: Float, orderLine: OrderLineData): Float {
        return drawTextView(
            xPos = xPos,
            yPos = yPos,
            text = localizer.localize(orderLine.labelKey),
            backgroundColor = ThemeColor.SemanticColor.layer_1.color.toArgb(),
            textColor = ThemeColor.SemanticColor.text_tertiary.color.toArgb(),
        )
    }

    private fun Canvas.drawOrderSize(xPos: Float, yPos: Float, orderLine: OrderLineData) {
        drawTextView(
            xPos = xPos,
            yPos = yPos,
            text = "${orderLine.size}",
            backgroundColor = orderLine.lineColor,
            textColor = orderLine.textColor,
            horizontalPadding = 12f,
        )
    }

    private fun Canvas.drawTextView(
        xPos: Float,
        yPos: Float,
        text: String,
        @ColorInt backgroundColor: Int,
        @ColorInt textColor: Int,
        verticalPadding: Float = 12f,
        horizontalPadding: Float = 24f,
    ): Float {
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColor
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 30f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }

        val textWidth = textPaint.measureText(text)
        val textHeight = textPaint.descent() - textPaint.ascent()

        val top = yPos - (textHeight / 2) - verticalPadding
        val right = xPos + textWidth + (horizontalPadding * 2)
        val bottom = yPos + (textHeight / 2) + verticalPadding

        drawRect(Rect(xPos.toInt(), top.toInt(), right.toInt(), bottom.toInt()), backgroundPaint)
        drawText(
            text,
            xPos + horizontalPadding,
            yPos + textHeight / 2 + textPaint.descent() / 2 - verticalPadding,
            textPaint,
        )

        return right
    }
}
