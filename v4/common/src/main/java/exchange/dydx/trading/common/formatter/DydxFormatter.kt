package exchange.dydx.trading.common.formatter

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

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

    private val timeFormatter: SimpleDateFormat by lazy {
        SimpleDateFormat("HH:mm:ss", locale)
    }

    private val dateTimeFormatter: SimpleDateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale)
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
        val format = NumberFormat.getInstance(locale)
        return format.format(number)
    }

    fun localFormatted(number: Double?): String? {
        return if (number != null) {
            val formatter = NumberFormat.getInstance(locale)
            formatter.format(number)
        } else {
            null
        }
    }
    fun localFormatted(number: Double?, digits: Int): String? {
        return localFormatted(number?.toBigDecimal(), digits)
    }

    fun localFormatted(number: BigDecimal?, digits: Int): String? {
        if (number != null) {
            val decimalFormat = NumberFormat.getInstance(locale) as? DecimalFormat
            decimalFormat?.minimumFractionDigits = maxOf(digits, 0)
            decimalFormat?.maximumFractionDigits = maxOf(digits, 0)

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

    fun localFormatted(number: BigDecimal?, size: String): String? {
        val digits = digits(size)
        return localFormatted(number, digits)
    }

    private fun digits(size: String): Int {
        val pattern = Pattern.compile("\\.(\\d+)")

        val matcher = pattern.matcher(size)
        if (matcher.find()) {
            return matcher.group(1).length
        }
        return 2 // Default digits
    }

    fun dollar(number: Double?, size: String? = null): String? {
        return dollar(number = number?.toBigDecimal(), size = size)
    }

    fun dollar(number: BigDecimal?, size: String? = null): String? {
        return dollar(number, digits = digits(size ?: "0.01"))
    }

    fun dollar(number: Double?, digits: Int): String? {
        return dollar(number = number?.toBigDecimal(), digits = digits)
    }

    fun dollar(number: BigDecimal?, digits: Int): String? {
        if (number == null) return null
        val formattedNumber = localFormatted(number.abs(), digits)
        return formattedNumber?.let {
            val rawDouble = raw(number.toDouble(), digits)?.toDouble() ?: 0.0
            if (rawDouble >= 0.0) {
                "$$it"
            } else {
                "-$$it"
            }
        }
    }

    fun percent(number: Double?, digits: Int, minDigits: Int? = null): String? {
        return percent(number = number?.toBigDecimal(), digits = digits, minDigits = minDigits)
    }

    fun percent(number: BigDecimal?, digits: Int, minDigits: Int? = null): String? {
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

    fun dateTime(time: Instant?): String? {
        return if (time != null) {
            dateTimeFormatter.format(Date.from(time))
        } else {
            null
        }
    }

    fun clock(time: Instant?): String? {
        return if (time != null) {
            timeFormatter.format(Date.from(time))
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
                percentFormatter.format(absoluteValue)?.plus("x")
            } else {
                "—"
            }
        }
    }

    /*
   xxxxx.yyyyy
   */
    fun decimalLocaleAgnostic(number: Double?, digits: Int? = null): String? {
        return raw(number = number, digits = digits, locale = Locale.US)
    }

    /*
     xxxxxx,yyyyy or xxxxx.yyyyy
     */
    fun raw(number: Double?, digits: Int? = null, locale: Locale = Locale.getDefault()): String? {
        return number?.let { value ->
            if (value.isFinite()) {
                if (digits != null) {
                    val rawFormatter = DecimalFormat.getInstance(locale).apply {
                        minimumFractionDigits = maxOf(digits, 0)
                        maximumFractionDigits = maxOf(digits, 0)
                        roundingMode = RoundingMode.HALF_UP
                        isGroupingUsed = false
                    }
                    val formatted = rawFormatter.format(number)
                    val number = rawFormatter.parse(formatted)
                    if (number.toDouble() == 0.0) { // handle -0.0
                        rawFormatter.format(0.0)
                    } else {
                        formatted
                    }
                } else {
                    BigDecimal(number).toPlainString()
                }
            } else {
                "∞"
            }
        }
    }
}
