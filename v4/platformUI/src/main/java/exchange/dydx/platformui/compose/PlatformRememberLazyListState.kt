package exchange.dydx.platformui.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect

private val scrollStateMap = mutableMapOf<String, ScrollState>()

private data class ScrollState(
    val index: Int,
    val scrollOffset: Int
)

@Composable
fun PlatformRememberLazyListState(
    key: String,
): LazyListState {
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        scrollStateMap[key]?.let {
            listState.animateScrollToItem(
                index = it.index,
                scrollOffset = it.scrollOffset,
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val lastIndex = listState.firstVisibleItemIndex
            val lastOffset = listState.firstVisibleItemScrollOffset
            scrollStateMap[key] = ScrollState(lastIndex, lastOffset)
        }
    }

    return listState
}
