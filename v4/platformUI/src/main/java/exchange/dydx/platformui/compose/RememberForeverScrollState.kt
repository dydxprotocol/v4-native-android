package exchange.dydx.platformui.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Static field, contains all scroll values
 */
private val SaveMap = mutableMapOf<String, ScrollKeyParams>()

private data class ScrollKeyParams(
    val value: Int
)

/**
 * Save scroll state on all time.
 * @param key value for comparing screen
 * @param initial see [ScrollState.value]
 */
@Composable
fun rememberForeverScrollState(
    key: String,
    initial: Int = 0
): ScrollState {
    val scrollState = rememberSaveable(saver = ScrollState.Saver) {
        val scrollValue: Int = SaveMap[key]?.value ?: initial
        SaveMap[key] = ScrollKeyParams(scrollValue)
        return@rememberSaveable ScrollState(scrollValue)
    }
    DisposableEffect(Unit) {
        onDispose {
            SaveMap[key] = ScrollKeyParams(scrollState.value)
        }
    }
    return scrollState
}