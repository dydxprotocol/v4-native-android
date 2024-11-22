package exchange.dydx.trading.feature.trade.orderbook.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
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
import exchange.dydx.utilities.utils.toDp
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.roundToInt

@Preview
@Composable
fun Preview_DydxOrderbookSideView() {
    DydxThemedPreviewSurface {
        DydxOrderbookSideView.Content(Modifier, DydxOrderbookSideView.ViewState.preview)
    }
}

object DydxOrderbookSideView {
    data class DydxOrderbookLine(
        val price: Double,
        val size: Double,
        val sizeText: String,
        val priceText: String,
        val depth: Double?,
        val taken: Double?,
        val textColor: ThemeColor.SemanticColor,
    )

    enum class DisplayStyle {
        TopDown, SideBySide;

        val intendedLineHeight: Dp
            get() = when (this) {
                TopDown -> 16.dp
                SideBySide -> 20.dp
            }

        val spacing: Dp
            get() = when (this) {
                TopDown -> 4.dp
                SideBySide -> 8.dp
            }
    }

    enum class Side {
        Asks, Bids;

        val barColor: ThemeColor.SemanticColor
            get() = when (this) {
                Asks -> ThemeColor.SemanticColor.negativeColor
                Bids -> ThemeColor.SemanticColor.positiveColor
            }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val lines: List<DydxOrderbookLine> = emptyList(),
        val maxDepth: Double = 0.0,
        val displayStyle: DisplayStyle = DisplayStyle.TopDown,
        val side: Side = Side.Asks,
        val onTap: (DydxOrderbookLine) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    fun AsksContent(
        modifier: Modifier,
        displayStyle: DisplayStyle = DisplayStyle.TopDown,
    ) {
        val viewModel: DydxOrderbookAsksViewModel = hiltViewModel()
        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state?.copy(displayStyle = displayStyle))
    }

    @Composable
    fun BidsContent(
        modifier: Modifier,
        displayStyle: DisplayStyle = DisplayStyle.TopDown,
    ) {
        val viewModel: DydxOrderbookBidsViewModel = hiltViewModel()
        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state?.copy(displayStyle = displayStyle))
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null || state.lines.isEmpty()) return

        var size by remember { mutableStateOf(IntSize.Zero) }

        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .rotate(
                    when (state.side) {
                        Side.Asks -> when (state.displayStyle) {
                            DisplayStyle.TopDown -> 180f
                            DisplayStyle.SideBySide -> 0f
                        }
                        Side.Bids -> 0f
                    },
                )
                .animateContentSize()
                .onSizeChanged {
                    size = it
                },
            userScrollEnabled = false,
            state = listState,
        ) {
            if (size == IntSize.Zero) {
                return@LazyColumn
            }

            // totalHeight = num * lineHeight + (num - 1) * spacing
            // num = (totalHeight + spacing) / (lineHeight + spacing)
            val numLinesToDisplay =
                floor(
                    (size.height.toDp + state.displayStyle.spacing) /
                        (state.displayStyle.intendedLineHeight + state.displayStyle.spacing),
                ).roundToInt()
            if (numLinesToDisplay <= 0) {
                return@LazyColumn
            }
            val actualLineHeight =
                (size.height.toDp - state.displayStyle.spacing * (numLinesToDisplay - 1) - 1.dp) / numLinesToDisplay

            for (i in 0 until numLinesToDisplay) {
                val line = state.lines.getOrNull(i)
                if (line != null) {
                    item(key = line.price) {
                        Box(
                            modifier = Modifier
                                .height(actualLineHeight)
                                .rotate(
                                    when (state.side) {
                                        Side.Asks -> when (state.displayStyle) {
                                            DisplayStyle.TopDown -> 180f
                                            DisplayStyle.SideBySide -> 0f
                                        }
                                        Side.Bids -> 0f
                                    },
                                )
                                .clickable {
                                    state.onTap(line)
                                }
                                .animateItemPlacement(),
                        ) {
                            CreateBar(
                                modifier = Modifier,
                                line = line,
                                size = size,
                                state = state,
                                actualLineHeight = actualLineHeight,
                            )

                            CreateTexts(
                                modifier = Modifier,
                                line = line,
                                state = state,
                            )
                        }

                        if (i < numLinesToDisplay - 1) {
                            Spacer(modifier = Modifier.height(state.displayStyle.spacing))
                        }
                    }
                } else {
                    item {
                        Spacer(modifier = Modifier.height(actualLineHeight).animateItemPlacement())
                        if (i < numLinesToDisplay - 1) {
                            Spacer(modifier = Modifier.height(state.displayStyle.spacing).animateItemPlacement())
                        }
                    }
                }
            }

            scope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    @Composable
    private fun BoxScope.CreateBar(
        modifier: Modifier,
        line: DydxOrderbookLine,
        size: IntSize,
        state: ViewState,
        actualLineHeight: Dp,
    ) {
        val width = size.width
        val depthRatio = ((line.depth ?: 0.0) / state.maxDepth).coerceAtMost(1.0)
        val sizeRatio = (line.size / state.maxDepth).coerceAtMost(1.0)
        val takenRatio = ((line.taken ?: 0.0) / state.maxDepth).coerceAtMost(1.0)

        Box(
            modifier = modifier
                .fillMaxSize()
                .rotate(
                    when (state.side) {
                        Side.Asks -> when (state.displayStyle) {
                            DisplayStyle.TopDown -> 0f
                            DisplayStyle.SideBySide -> 180f
                        }
                        Side.Bids -> 0f
                    },
                ),
        ) {
            Box(
                modifier = Modifier
                    .animateContentSize()
                    .size(width = (width * depthRatio).toInt().toDp, height = actualLineHeight)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(state.side.barColor.color.copy(alpha = 0.2f))
                    .zIndex(0f),
            )

            Box(
                modifier = Modifier
                    .animateContentSize()
                    .size(width = (width * sizeRatio).toInt().toDp, height = actualLineHeight)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(state.side.barColor.color.copy(alpha = 0.6f))
                    .zIndex(1f),
            )

            Box(
                modifier = Modifier
                    .animateContentSize()
                    .size(width = (width * takenRatio).toInt().toDp, height = actualLineHeight)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(state.side.barColor.color.copy(alpha = 0.9f))
                    .zIndex(2f),
            )
        }
    }

    @Composable
    private fun BoxScope.CreateTexts(
        modifier: Modifier,
        line: DydxOrderbookLine,
        state: ViewState,
    ) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = ThemeShapes.VerticalPadding)
                .zIndex(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = line.sizeText,
                modifier = Modifier,
                style = TextStyle.dydxDefault
                    .themeColor(line.textColor)
                    .themeFont(
                        fontSize = ThemeFont.FontSize.mini,
                        fontType = ThemeFont.FontType.number,
                    ),
            )

            Text(
                text = line.priceText,
                modifier = Modifier,
                style = TextStyle.dydxDefault
                    .themeColor(line.textColor)
                    .themeFont(
                        fontSize = ThemeFont.FontSize.mini,
                        fontType = ThemeFont.FontType.number,
                    ),
            )
        }
    }
}
