package exchange.dydx.trading.feature.shared.views

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

@Preview
@Composable
fun Preview_IntervalText() {
    DydxThemedPreviewSurface {
        IntervalText.Content(Modifier, IntervalText.ViewState.preview)
    }
}

object IntervalText {

    enum class Direction {
        COUNT_DOWN, COUNT_UP, COUNT_DOWN_TO_HOUR
    }

    enum class Format {
        FULL, SHORT
    }

    data class ViewState(
        var date: Instant? = null,
        val direction: Direction = Direction.COUNT_UP,
        val format: Format = Format.SHORT,
        val formatter: DydxFormatter,
    ) {
        companion object {
            val preview = ViewState(
                formatter = DydxFormatter(),
            )
        }

        var dateText: MutableStateFlow<String?> = MutableStateFlow(null)

        private var timer: Timer? = null

        init {
            resetTimer()
        }

        private fun resetTimer() {
            timer?.cancel()
            dateText.value = null

            if (direction == Direction.COUNT_DOWN_TO_HOUR) {
                val now = Instant.now()
                if (date == null || now > date) {
                    date = now.truncatedTo(ChronoUnit.HOURS).plusSeconds(3600)
                }
            }

            val timerInterval: Long? = when (format) {
                Format.SHORT -> timerInterval
                Format.FULL -> 1000 // 1 second for full format
            }

            timerInterval?.let {
                displayDate()
                timer?.cancel()
                timer = fixedRateTimer(initialDelay = 0, period = it) {
                    val now = Instant.now()
                    if (direction == Direction.COUNT_DOWN_TO_HOUR && now > date) {
                        date = now.truncatedTo(ChronoUnit.HOURS).plusSeconds(3600)
                    }
                    displayDate()
                }
            }
        }

        private fun displayDate() {
            if (timerInterval != null) {
                dateText.value = when (format) {
                    Format.SHORT -> formatter.interval(date)
                    Format.FULL -> formatter.time(date)
                }
            } else {
                dateText.value = null
            }
        }

        private val timerInterval: Long?
            get() {
                val currentDate = date ?: return null

                val now = Instant.now()
                val interval: Long = when (direction) {
                    Direction.COUNT_UP -> currentDate.until(now, ChronoUnit.SECONDS)
                    Direction.COUNT_DOWN, Direction.COUNT_DOWN_TO_HOUR -> now.until(currentDate, ChronoUnit.SECONDS)
                }

                return when {
                    interval in 1..60 -> 1000 // 1 second
                    interval <= 60 * 60 -> 60 * 1000 // 1 minute
                    else -> 24 * 60 * 60 * 1000 // 1 day
                }
            }
    }

    @Composable
    fun Content(
        modifier: Modifier,
        state: ViewState?,
        textStyle: TextStyle = TextStyle.dydxDefault,
    ) {
        if (state == null) {
            return
        }

        val dateText = state.dateText.collectAsState().value

        Text(
            text = dateText ?: "",
            style = textStyle,
            modifier = modifier,
        )
    }
}
