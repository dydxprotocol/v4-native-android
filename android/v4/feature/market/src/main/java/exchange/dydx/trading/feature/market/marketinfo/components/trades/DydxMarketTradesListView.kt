package exchange.dydx.trading.feature.market.marketinfo.components.trades

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.utilities.utils.toDp
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.roundToInt

@Preview
@Composable
fun Preview_DydxMarketTradesView() {
    DydxThemedPreviewSurface {
        DydxMarketTradesListView.Content(
            Modifier,
            DydxMarketTradesListView.ViewState.preview,
            null,
        )
    }
}

object DydxMarketTradesListView : DydxComponent {
    data class ViewState(
        val trades: List<DydxMarketTradeItemView.ViewState>?,
    ) {
        companion object {
            val preview = ViewState(
                trades = listOf(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketTradesListViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        val headerState = viewModel.headerState
        Content(modifier, state, headerState)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?, headerState: DydxMarketTradesHeaderView.ViewState?) {
        if (state?.trades == null || state.trades.isEmpty()) return

        var size by remember { mutableStateOf(IntSize.Zero) }

        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            headerState?.let {
                Column(
                    modifier = Modifier,
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DydxMarketTradesHeaderView.Content(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp),
                        state = it,
                    )
                }
            }
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .onSizeChanged {
                        size = it
                    },
                userScrollEnabled = true,
                state = listState,
            ) {
                if (size == IntSize.Zero) {
                    return@LazyColumn
                }
                val desiredHeight = 20.dp // bar height
                val desiredSpacing = 16.dp // spacing between bars
                val dividerHeight = 1.dp

                val visibleLinesToDisplay =
                    floor(
                        (size.height.toDp) /
                            (desiredHeight + desiredSpacing + dividerHeight),
                    ).roundToInt()
                val actualLineHeight =
                    size.height.toDp / visibleLinesToDisplay - desiredSpacing - dividerHeight

                for (trade in state.trades) {
                    item(key = trade.id) {
                        Spacer(modifier = Modifier.height(desiredSpacing / 2))
                        DydxMarketTradeItemView.Content(
                            modifier = Modifier
                                .fillMaxSize()
                                .height(actualLineHeight)
                                .animateItemPlacement(),
                            state = trade,
                        )
                        Spacer(modifier = Modifier.height(desiredSpacing / 2))
                        if (trade != state.trades.last()) {
                            PlatformDivider()
                        }
                    }
                }

                scope.launch {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }
}
