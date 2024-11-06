package exchange.dydx.trading.common.formatter

import exchange.dydx.utilities.utils.rounded
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.round

@Singleton
class DydxFormatter @Inject constructor() {

    var locale = Locale.getDefault()

    private val percentFormatter: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(locale)
//        symbols.groupingSeparator = ','
//        symbols.decimalSeparator = '.'
        DecimalFormat("0.00%", symbols)
    }

    private val dateFormatter: SimpleDateFormat by lazy {
        SimpleDateFormat("MMM dd", locale)
    }

    private val utcDateFormatter: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern("MMM dd").withZone(ZoneOffset.UTC)
    }

    private val timeFormatter: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern("HH:mm:ss", locale)
    }

    private val dateTimeFormatter: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", locale)
    }

    fun dollarVolume(number: Double?, digits: Int = 2): String? {
        return dollarVolume(number?.toBigDecimal(), digits)
    }

    fun dollarVolume(number: BigDecimal?, digits: Int = 2): String? {
        if (number != null) {
            val formatted = condensed(number.abs(), digits)
            if (formatted != null) {
                return if (number >= BigDecimal.ZERO) {
                    "$$formatted"
                } else {
                    "-$$formatted"
                }
            }
        }
        return null
    }

    fun condensed(number: Double?, digits: Int = 4): String? {
        return condensed(number?.toBigDecimal(), digits)
    }

    fun condensed(number: BigDecimal?, digits: Int = 4): String? {
        if (number != null) {
            val postfix = arrayOf("", "K", "M", "B", "T")
            var value = number.toDouble().absoluteValue
            var index = 0
            while (value > 1000.0 && index < (postfix.size - 1)) {
                value /= 1000.0
                index++
            }
            val formatted = BigDecimal(value).setScale(digits, RoundingMode.HALF_UP)
            val numberString = formatBigDecimal(formatted)
            return if (number >= BigDecimal.ZERO) "$numberString${postfix[index]}" else "-$numberString${postfix[index]}"
        }
        return null
    }

    fun formatBigDecimal(number: BigDecimal?): String? {
        if (number == null) return null
        val formatter = NumberFormat.getInstance(locale)
        formatter.maximumFractionDigits = 99 // so that it doesn't use scientific notation
        return formatter.format(number)
    }

    fun localFormatted(number: Double?): String? {
        return if (number != null) {
            val formatter = NumberFormat.getInstance(locale)
            formatter.maximumFractionDigits = 99 // so that it doesn't use scientific notation
            formatter.format(number)
        } else {
            null
        }
    }
    fun localFormatted(number: Double?, digits: Int?): String? {
        if (number != null) {
            val number = if (digits != null) rounded(number, digits) else number
            val decimalFormat = NumberFormat.getInstance(locale) as? DecimalFormat
            if (digits != null) {
                decimalFormat?.minimumFractionDigits = maxOf(digits, 0)
                decimalFormat?.maximumFractionDigits = maxOf(digits, 0)
            }

            val formatted = decimalFormat?.format(number)
            val parsed = decimalFormat?.parse(formatted)
            if (parsed?.toDouble() == 0.0) { // handle -0.0
                return decimalFormat.format(0.0)
            } else {
                return formatted
            }
        }
        return null
    }

    fun dollar(number: Double?, size: Double? = null): String? {
        return dollar(number = number, digits = digits(size))
    }

    fun dollar(number: Double?, digits: Int?): String? {
        if (number == null) return null
        val formattedNumber = localFormatted(abs(number), digits)
        return formattedNumber?.let {
            val rounded = if (digits != null && digits >= 0) number.rounded(toPlaces = digits) else number
            if (rounded >= 0) {
                "$$it"
            } else {
                "-$$it"
            }
        }
    }

    fun percent(number: Double?, digits: Int, minDigits: Int? = null): String? {
        if (number != null) {
            percentFormatter.minimumFractionDigits = minDigits ?: digits
            percentFormatter.maximumFractionDigits = digits
            val formatted = percentFormatter.format(number)
            return "$formatted"
        } else {
            return null
        }
    }

    fun interval(time: Instant?): String? {
        return if (time != null) {
            val currentTime = Instant.now()
            var interval = time.until(currentTime, ChronoUnit.SECONDS)
            if (interval < 0) {
                interval *= -1
            }

            if (Math.abs(interval) > 24 * 3600) {
                String.format("%dd", interval / (24 * 3600))
            } else if (Math.abs(interval) > 3600) {
                String.format("%dh", interval / 3600)
            } else if (Math.abs(interval) > 60) {
                String.format("%dm", interval / 60)
            } else {
                String.format("%ds", interval)
            }
        } else {
            null
        }
    }

    fun time(time: Instant?): String? {
        if (time != null) {
            val currentTime = Instant.now()
            var interval = time.until(currentTime, ChronoUnit.SECONDS)
            if (interval < 0) {
                interval *= -1
            }

            if (interval >= 3600) {
                val hour = interval / 3600
                val min = (interval % 3600) / 60
                return String.format("%02dh %02dm", hour, min)
            } else {
                val min = interval / 60
                val sec = interval % 60
                return String.format("%02dm %02ds", min, sec)
            }
        } else {
            return null
        }
    }

    fun date(time: Instant?): String? {
        return if (time != null) {
            dateFormatter.format(Date.from(time))
        } else {
            null
        }
    }

    fun utcDate(time: Instant?): String? {
        return if (time != null) {
            utcDateFormatter.format(time)
        } else {
            null
        }
    }

    fun dateTime(
        time: Instant?,
        timeZone: ZoneId = ZoneId.systemDefault(),
        formatter: DateTimeFormatter = dateTimeFormatter
    ): String? {
        return if (time != null) {
            val ldt: LocalDateTime = time.atZone(timeZone).toLocalDateTime()
            formatter.format(ldt)
        } else {
            null
        }
    }

    fun clock(time: Instant?, timeZone: ZoneId = ZoneId.systemDefault()): String? {
        return if (time != null) {
            val ldt: LocalDateTime = time.atZone(timeZone).toLocalDateTime()
            timeFormatter.format(ldt)
        } else {
            null
        }
    }

    fun leverage(number: Double?, digits: Int = 2): String? {
        return number?.let {
            val absoluteValue = it.absoluteValue
            if (absoluteValue.isFinite()) {
                val percentFormatter = NumberFormat.getNumberInstance().apply {
                    minimumFractionDigits = digits
                    maximumFractionDigits = digits
                }
                percentFormatter.format(absoluteValue).plus("x")
            } else {
                "—"
            }
        }
    }

    /*
    xxxxx.yyyyy

      Will take the number and round it to the closest step size
      e.g. if number is 1021 and step size is "100" then output is "1000"
    */
    fun decimalLocaleAgnostic(number: Double?, size: Double? = null): String? {
        return raw(number = number, size = size, locale = Locale.US)
    }

    /*
   xxxxx.yyyyy

   */
    fun decimalLocaleAgnostic(number: Double?, digits: Int?): String? {
        return raw(number = number, digits = digits, locale = Locale.US)
    }

    /*
     xxxxxx,yyyyy or xxxxx.yyyyy

      Will take the number and round it to the closest step size
      e.g. if number is 1021 and step size is "100" then output is "1000"
     */
    fun raw(number: Double?, size: Double? = null, locale: Locale? = null): String? {
        val digits = digits(size)
        return raw(number = number, digits = digits, locale = locale ?: this.locale)
    }

    /*
     xxxxxx,yyyyy or xxxxx.yyyyy
     */
    fun raw(
        number: Double?,
        digits: Int?,
        locale: Locale = Locale.getDefault(),
        rounding: RoundingMode = RoundingMode.HALF_UP
    ): String? {
        return number?.let { value ->
            if (value.isFinite()) {
                if (digits != null) {
                    val rounded = rounded(value, digits)
                    val rawFormatter = DecimalFormat.getInstance(locale).apply {
                        minimumFractionDigits = maxOf(digits, 0)
                        maximumFractionDigits = maxOf(digits, 0)
                        roundingMode = rounding
                        isGroupingUsed = false
                    }
                    val formatted = rawFormatter.format(rounded)
                    val number = rawFormatter.parse(formatted)
                    if (number.toDouble() == 0.0) { // handle -0.0
                        rawFormatter.format(0.0)
                    } else {
                        formatted
                    }
                } else {
                    number.toBigDecimal().toPlainString()
                }
            } else {
                "∞"
            }
        }
    }

    private fun rounded(number: Double, digits: Int): Double {
        if (digits >= 0) {
            return number
        } else {
            val reversed = digits * -1
            val divideBy = Math.pow(10.0, reversed.toDouble() - 1)
            return round(number / divideBy).toInt() * divideBy
        }
    }

    /*
       Returns the number of digits for a given size specified in the format of 10^(-x)
         e.g.
         0.001 -> 3,
         0.1 -> 1,
         1 -> 0,
         10 -> -1
         1000 -> -3
     */
    private fun digits(size: Double?): Int? {
        if (size == null || size <= 0.0) return null

        var size = size
        if (size >= 1) {
            var count = 0
            while (size >= 1) {
                count++
                size /= 10
            }
            return count * -1
        } else if (size <= 0.1) {
            var count = 0
            while (size <= 0.1) {
                count++
                size *= 10
            }
            return count
        } else {
            return null
        }
    }
}
