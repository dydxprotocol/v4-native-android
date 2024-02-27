package exchange.dydx.trading.feature.market.marketinfo.components.prices

import exchange.dydx.platformui.components.charts.formatter.ValueAxisFormatter
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

class DateTimeAxisFormatter(
    private val anchorDateTime: Instant,
    private val candlesPeriod: String?,
    private val offset: Int,
) : ValueAxisFormatter() {
    private val minuteFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val hourFormatter = DateTimeFormatter.ofPattern("HH")
    private val dayFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH)

    override fun getFormattedValue(value: Float): String {
        val datetime = when (candlesPeriod) {
            "1DAY" -> {
                anchorDateTime.plusSeconds((value.toLong() - offset) * 3600 * 24)
            }

            "1HOURS" -> {
                anchorDateTime.plusSeconds((value.toLong() - offset) * 3600)
            }

            "4HOURS" -> {
                anchorDateTime.plusSeconds(((value * 4).toLong() - offset) * 3600)
            }

            "1MIN" -> {
                anchorDateTime.plusSeconds((value.toLong() - offset) * 60)
            }

            "5MINS" -> {
                anchorDateTime.plusSeconds(((value * 5).toLong() - offset) * 60)
            }

            "15MINS" -> {
                anchorDateTime.plusSeconds(((value * 15).toLong() - offset) * 60)
            }

            "30MINS" -> {
                anchorDateTime.plusSeconds(((value * 30).toLong() - offset) * 60)
            }

            else -> {
                anchorDateTime.plusSeconds((value.toLong() - offset) * 60)
            }
        }.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
        return when (candlesPeriod) {
            "1DAY" -> {
                datetime.format(dayFormatter)
            }

            "1HOUR", "4HOURS" -> {
                datetime.format(hourFormatter)
            }

            else -> {
                datetime.format(minuteFormatter)
            }
        }
    }
}
