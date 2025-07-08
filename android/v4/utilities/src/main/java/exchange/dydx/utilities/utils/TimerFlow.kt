package exchange.dydx.utilities.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

fun timerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
    delay(initialDelay)
    while (true) {
        emit(Unit)
        delay(period)
    }
}

fun delayFlow(duration: Duration) = flow {
    delay(duration)
    emit(Unit)
}
