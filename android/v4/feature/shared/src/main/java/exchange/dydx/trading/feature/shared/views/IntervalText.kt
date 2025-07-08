package exchange.dydx.trading.feature.shared.views

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.scheduleAtFixedRate

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
    ) {
        companion object {
            val preview = ViewState(
                date = Instant.now(),
                direction = Direction.COUNT_UP,
                format = Format.SHORT,
            )
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

        val viewModel = remember { IntervalTextViewModel(formatter = DydxFormatter()) }
        LaunchedEffect(Unit) {
            viewModel.start(state.date, state.direction, state.format)
        }
        val dateText = viewModel.dateText.collectAsState().value

        Text(
            text = dateText ?: "",
            style = textStyle,
            modifier = modifier,
        )
    }
}

@HiltViewModel
class IntervalTextViewModel @Inject constructor(
    val formatter: DydxFormatter,
) : ViewModel() {
    val dateText: MutableStateFlow<String?> = MutableStateFlow(null)

    private var timer: Timer? = null

    private var date: Instant? = null
    private var direction: IntervalText.Direction = IntervalText.Direction.COUNT_UP
    private var format: IntervalText.Format = IntervalText.Format.SHORT

    override fun onCleared() {
        stopTimer()
    }

    fun start(date: Instant?, direction: IntervalText.Direction, format: IntervalText.Format) {
        this.date = date
        this.direction = direction
        this.format = format

        startTimer()
    }

    private fun startTimer() {
        dateText.value = null

        if (direction == IntervalText.Direction.COUNT_DOWN_TO_HOUR) {
            val now = Instant.now()
            if (date == null || now > date) {
                date = now.truncatedTo(ChronoUnit.HOURS).plusSeconds(3600)
            }
        }

        val timerInterval: Long? = when (format) {
            IntervalText.Format.SHORT -> timerInterval
            IntervalText.Format.FULL -> 1000 // 1 second for full format
        }

        timerInterval?.let {
            displayDate()
            stopTimer()
            timer = Timer()
            timer?.scheduleAtFixedRate(delay = 0, period = it) {
                val now = Instant.now()
                if (direction == IntervalText.Direction.COUNT_DOWN_TO_HOUR && now > date) {
                    date = now.truncatedTo(ChronoUnit.HOURS).plusSeconds(3600)
                }
                displayDate()
            }
        }
    }

    private fun stopTimer() {
        timer?.cancel()
        timer?.purge()
        timer = null
    }

    private fun displayDate() {
        if (timerInterval != null) {
            dateText.value = when (format) {
                IntervalText.Format.SHORT -> formatter.interval(date)
                IntervalText.Format.FULL -> formatter.time(date)
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
                IntervalText.Direction.COUNT_UP -> currentDate.until(now, ChronoUnit.SECONDS)
                IntervalText.Direction.COUNT_DOWN, IntervalText.Direction.COUNT_DOWN_TO_HOUR -> now.until(currentDate, ChronoUnit.SECONDS)
            }

            return when {
                interval in 1..60 -> 1000 // 1 second
                interval <= 60 * 60 -> 60 * 1000 // 1 minute
                else -> 24 * 60 * 60 * 1000 // 1 day
            }
        }
}
