package exchange.dydx.trading.feature.market.marketinfo.components.prices

import exchange.dydx.platformui.components.charts.formatter.ValueAxisFormatter
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

class DateTimeAxisFormatter(
    private val anchorDateTime: Instant,
    private val candlesPeriod: CandlePeriod?,
    private val offset: Int,
) : ValueAxisFormatter() {
    private val minuteFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val hourFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dayFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH)

    override fun getFormattedValue(value: Float): String {
        val datetime = when (candlesPeriod) {
            CandlePeriod.OneDay -> {
                anchorDateTime.plusSeconds((value.toLong() - offset) * 3600 * 24)
            }

            CandlePeriod.OneHour -> {
                anchorDateTime.plusSeconds((value.toLong() - offset) * 3600)
            }

            CandlePeriod.FourHours -> {
                anchorDateTime.plusSeconds(((value * 4).toLong() - offset) * 3600)
            }

            CandlePeriod.OneMinute -> {
                anchorDateTime.plusSeconds((value.toLong() - offset) * 60)
            }

            CandlePeriod.FiveMinutes -> {
                anchorDateTime.plusSeconds(((value * 5).toLong() - offset) * 60)
            }

            CandlePeriod.FifteenMinutes -> {
                anchorDateTime.plusSeconds(((value * 15).toLong() - offset) * 60)
            }

            CandlePeriod.ThirtyMinutes -> {
                anchorDateTime.plusSeconds(((value * 30).toLong() - offset) * 60)
            }

            else -> {
                anchorDateTime.plusSeconds((value.toLong() - offset) * 60)
            }
        }.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
        return when (candlesPeriod) {
            CandlePeriod.OneDay, CandlePeriod.FourHours -> {
                datetime.format(dayFormatter)
            }

            CandlePeriod.OneHour -> {
                datetime.format(hourFormatter)
            }

            else -> {
                datetime.format(minuteFormatter)
            }
        }
    }
}
