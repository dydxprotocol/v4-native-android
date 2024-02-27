package exchange.dydx.trading.common.compose

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

private const val StopTimeoutMillis: Long = 5000
private const val TAG = "CoroutineUtils"

/**
 * A [SharingStarted] meant to be used with a [StateFlow] to expose data to the UI.
 *
 * When the UI stops observing, upstream flows stay active for some time to allow the system to
 * come back from a short-lived configuration change (such as rotations). If the UI stops
 * observing for longer, the cache is kept but the upstream flows are stopped. When the UI comes
 * back, the latest value is replayed and the upstream flows are executed again. This is done to
 * save resources when the app is in the background but let users switch between apps quickly.
 */
val WhileUiSubscribed: SharingStarted = SharingStarted.WhileSubscribed(StopTimeoutMillis)

val dydxHandler = CoroutineExceptionHandler {
        _, e ->
    if (e is java.util.concurrent.CancellationException) {
        throw e
    }
    Timber.tag(TAG).e(e, "Unhandled exception in coroutine: ")
}
