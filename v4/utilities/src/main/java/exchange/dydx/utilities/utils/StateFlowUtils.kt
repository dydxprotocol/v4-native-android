package exchange.dydx.utilities.utils

import com.hoc081098.flowext.ThrottleConfiguration
import com.hoc081098.flowext.throttleTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

fun <T, R> StateFlow<T>.mapState(coroutineScope: CoroutineScope, map: (T) -> R): StateFlow<R> =
    map { map(it) }
        .stateIn(coroutineScope, SharingStarted.Lazily, initialValue = map(value))

@OptIn(ExperimentalCoroutinesApi::class)
fun <T, R> StateFlow<T>.mapStateWithThrottle(coroutineScope: CoroutineScope, map: (T) -> R): StateFlow<R> =
    map { map(it) }
        .throttleTime(10, throttleConfiguration = ThrottleConfiguration.LEADING_AND_TRAILING)
        .stateIn(coroutineScope, SharingStarted.Lazily, initialValue = map(value))

fun <A, B, R> combineState(
    stateFlowA: StateFlow<A>,
    stateFlowB: StateFlow<B>,
    coroutineScope: CoroutineScope,
    combineState: (A, B) -> R
): StateFlow<R> {
    return combine(
        stateFlowA,
        stateFlowB,
    ) { a: A, b: B ->
        combineState(a, b)
    }.stateIn(coroutineScope, SharingStarted.Lazily, initialValue = combineState(stateFlowA.value, stateFlowB.value))
}
