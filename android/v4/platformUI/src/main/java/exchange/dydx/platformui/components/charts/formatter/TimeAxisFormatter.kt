package exchange.dydx.platformui.components.charts.formatter

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class DateTimeResolution(val rawValue: Int) {
    MINUTE(rawValue = 0),
    HOUR(rawValue = 1),
    DATE(rawValue = 2);

    companion object {
        operator fun invoke(rawValue: Int): DateTimeResolution? = values().firstOrNull { it.rawValue == rawValue }
    }
}

class TimeAxisFormatter(val resolution: DateTimeResolution) : ValueAxisFormatter() {
    private val minuteFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val hourFormatter = DateTimeFormatter.ofPattern("HH")
    private val dayFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH)
    override fun getFormattedValue(value: Float): String {
        val datetime = Instant.ofEpochSecond(value.toLong() / 1000).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
        return when (resolution) {
            DateTimeResolution.MINUTE, -> {
                datetime.format(minuteFormatter)
            }

            DateTimeResolution.HOUR -> {
                datetime.format(hourFormatter)
            }

            DateTimeResolution.DATE -> {
                datetime.format(dayFormatter)
            }
        }
    }
}
