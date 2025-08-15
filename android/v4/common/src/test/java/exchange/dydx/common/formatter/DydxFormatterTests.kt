package exchange.dydx.common.formatter

import exchange.dydx.trading.common.formatter.DydxFormatter
import org.junit.Assert
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class DydxFormatterTests {

    @Test
    fun testDollarVolume() {
        val formatter = DydxFormatter()

        data class TestCase(
            val number: Double,
            val digits: Int = 2,
            val expected: String,
        )

        val testCases = listOf(
            TestCase(number = 123.0, digits = 2, expected = "$123"),
            TestCase(number = 1234.0, digits = 2, expected = "$1.23K"),
            TestCase(number = 1234567.0, digits = 2, expected = "$1.23M"),
            TestCase(number = 12345678999.0, digits = 2, expected = "$12.35B"),
            TestCase(number = -12345678999.0, digits = 2, expected = "-$12.35B"),
        )

        testCases.forEach { testCase ->
            val formatted = formatter.dollarVolume(number = testCase.number, digits = testCase.digits)
            assert(formatted == testCase.expected) { "Test case: $testCase, formatted: $formatted" }
        }
    }

    @Test
    fun testCondensed() {
        val formatter = DydxFormatter()

        data class TestCase(
            val number: Double,
            val digits: Int = 2,
            val expected: String,
        )

        val testCases = listOf(
            TestCase(number = 123.0, digits = 2, expected = "123"),
            TestCase(number = 1234.0, digits = 2, expected = "1.23K"),
            TestCase(number = 1234567.0, digits = 2, expected = "1.23M"),
            TestCase(number = 12345678999.0, digits = 2, expected = "12.35B"),
            TestCase(number = -12345678999.0, digits = 2, expected = "-12.35B"),
        )

        testCases.forEach { testCase ->
            val formatted = formatter.condensed(number = testCase.number, digits = testCase.digits)
            assert(formatted == testCase.expected) { "Test case: $testCase, formatted: $formatted" }
        }
    }

    @Test
    fun testLocalFormatted() {
        val formatter = DydxFormatter()

        data class TestCase(
            val number: Double,
            val digits: Int = 2,
            val expected: String,
        )

        val testCases = listOf(
            TestCase(number = 1.0, digits = 2, expected = "1.00"),
            TestCase(number = 0.5, digits = 2, expected = "0.50"),
            TestCase(number = -0.25, digits = 2, expected = "-0.25"),
            TestCase(number = -0.0002, digits = 2, expected = "0.00"),
            TestCase(number = 0.0, digits = 2, expected = "0.00"),
            TestCase(number = 0.0, digits = 0, expected = "0"),
            TestCase(number = 0.0, digits = 1, expected = "0.0"),
            TestCase(number = 0.6, digits = 0, expected = "1"),
            TestCase(number = 12345.003, digits = 2, expected = "12,345.00"),
        )

        testCases.forEach { testCase ->
            val formatted = formatter.localFormatted(number = testCase.number, digits = testCase.digits)
            assert(formatted == testCase.expected) { "Test case: $testCase, formatted: $formatted" }
        }

        formatter.locale = Locale.FRANCE
        val formatted = formatter.localFormatted(number = 1123345.123, digits = 2)
        Assert.assertEquals("1 123 345,12", formatted)
    }

    @Test
    fun testDollar() {
        val formatter = DydxFormatter()

        data class TestCase(
            val number: Double,
            val digits: Int = 2,
            val expected: String,
            val locale: Locale = Locale.US,
        )

        val testCases = listOf(
            TestCase(number = 1.0, digits = 2, expected = "$1.00"),
            TestCase(number = 0.5, digits = 2, expected = "$0.50"),
            TestCase(number = -0.25, digits = 2, expected = "-$0.25"),
            TestCase(number = -0.0002, digits = 2, expected = "$0.00"),
            TestCase(number = 0.0, digits = 2, expected = "$0.00"),
            TestCase(number = 0.0, digits = 0, expected = "$0"),
            TestCase(number = 0.0, digits = 1, expected = "$0.0"),
            TestCase(number = 0.6, digits = 0, expected = "$1"),
            TestCase(number = 1.0, digits = 2, expected = "$1,00", locale = Locale.FRANCE),
            TestCase(number = 0.5, digits = 2, expected = "$0,50", locale = Locale.FRANCE),
            TestCase(number = -0.25, digits = 2, expected = "-$0,25", locale = Locale.FRANCE),
            TestCase(number = -0.0002, digits = 2, expected = "$0,00", locale = Locale.FRANCE),
            TestCase(number = 0.0, digits = 2, expected = "$0,00", locale = Locale.FRANCE),
            TestCase(number = 0.0, digits = 0, expected = "$0", locale = Locale.FRANCE),
            TestCase(number = 0.0, digits = 1, expected = "$0,0", locale = Locale.FRANCE),
            TestCase(number = 0.6, digits = 0, expected = "$1", locale = Locale.FRANCE),
        )

        testCases.forEach { testCase ->
            formatter.locale = testCase.locale
            val formatted = formatter.dollar(number = testCase.number, digits = testCase.digits)
            assert(formatted == testCase.expected) { "Test case: $testCase, formatted: $formatted" }
        }
    }

    @Test
    fun testDollar_DoubleSize() {
        val formatter = DydxFormatter()

        data class TestCase(
            val number: Double,
            val size: Double?,
            val expected: String,
        )

        val testCases = listOf(
            TestCase(number = 0.6, size = 1.0, expected = "$1"),
            TestCase(number = 1.0, size = 0.01, expected = "$1.00"),
            TestCase(number = 0.5, size = 0.01, expected = "$0.50"),
            TestCase(number = -0.25, size = 0.01, expected = "-$0.25"),
            TestCase(number = -0.0002, size = 0.01, expected = "$0.00"),
            TestCase(number = 0.0, size = 0.01, expected = "$0.00"),
            TestCase(number = 0.0, size = 1.0, expected = "$0"),
            TestCase(number = 0.0, size = 0.1, expected = "$0.0"),
        )

        testCases.forEach { testCase ->
            val formatted = formatter.dollar(number = testCase.number, size = testCase.size)
            assert(formatted == testCase.expected) { "Test case: $testCase, formatted: $formatted" }
        }
    }

    @Test
    fun testPercent() {
        val formatter = DydxFormatter()

        data class TestCase(
            val number: Double,
            val digits: Int = 2,
            val expected: String,
        )

        val testCases = listOf(
            TestCase(number = 1.0, digits = 2, expected = "100.00%"),
            TestCase(number = 0.523, digits = 2, expected = "52.30%"),
            TestCase(number = -0.25, digits = 2, expected = "-25.00%"),
            TestCase(number = 0.0, digits = 2, expected = "0.00%"),
            TestCase(number = 0.0, digits = 0, expected = "0%"),
            TestCase(number = 0.0, digits = 1, expected = "0.0%"),
            TestCase(number = 0.6, digits = 0, expected = "60%"),
        )

        testCases.forEach { testCase ->
            val formatted = formatter.percent(number = testCase.number, digits = testCase.digits)
            assert(formatted == testCase.expected) { "Test case: $testCase, formatted: $formatted" }
        }
    }

    @Test
    fun testInterval() {
        val formatter = DydxFormatter()
        var time = formatter.interval(Date().toInstant().minusSeconds(10))
        Assert.assertEquals("10s", time)

        time = formatter.interval(Date().toInstant().minusSeconds(90))
        Assert.assertEquals("1m", time)

        time = formatter.interval(Date().toInstant().minusSeconds(3610))
        Assert.assertEquals("1h", time)

        time = formatter.interval(Date().toInstant().minusSeconds(24 * 3601 + 10))
        Assert.assertEquals("1d", time)
    }

//    @Test
//    fun testDate() {
//        val formatter = DydxFormatter()
//        val time = formatter.date(Instant.parse("2021-10-01T00:00:00Z"))
//        Assert.assertEquals("Sep 30", time)
//    }

    @Test
    fun testUtcDate() {
        val formatter = DydxFormatter()
        val time = formatter.utcDate(Instant.parse("2021-10-01T00:00:00Z"))
        Assert.assertEquals("Oct 01", time)
    }

    @Test
    fun testDateTime() {
        val formatter = DydxFormatter()
        val time = formatter.dateTime(Instant.parse("2021-10-01T00:00:00Z"), ZoneId.of("America/Los_Angeles"))
        Assert.assertEquals("2021-09-30 17:00:00", time)
    }

    @Test
    fun testClock() {
        val formatter = DydxFormatter()
        val time = formatter.clock(Instant.parse("2021-10-01T00:00:00Z"), ZoneId.of("America/Los_Angeles"))
        Assert.assertEquals("17:00:00", time)
    }

    @Test
    fun testLeverage() {
        val formatter = DydxFormatter()

        data class TestCase(
            val number: Double,
            val digits: Int = 2,
            val expected: String,
        )

        val testCases = listOf(
            TestCase(number = 1.0, digits = 2, expected = "1.00x"),
            TestCase(number = 0.5, digits = 2, expected = "0.50x"),
            TestCase(number = -0.25, digits = 2, expected = "0.25x"),
            TestCase(number = 0.0, digits = 2, expected = "0.00x"),
            TestCase(number = 0.0, digits = 0, expected = "0x"),
            TestCase(number = 0.0, digits = 1, expected = "0.0x"),
            TestCase(number = 0.6, digits = 0, expected = "1x"),
        )

        testCases.forEach { testCase ->
            val formatted = formatter.leverage(number = testCase.number, digits = testCase.digits)
            assert(formatted == testCase.expected) { "Test case: $testCase, formatted: $formatted" }
        }
    }

    @Test
    fun testDecimalLocaleAgnostic() {
        val formatter = DydxFormatter()
        data class TestCase(
            val number: Double,
            val digits: Int,
            val expected: String,
        )

        val testCases = listOf(
            TestCase(number = 1.0, digits = 2, expected = "1.00"),
            TestCase(number = -0.001, digits = 0, expected = "0"),
            TestCase(number = -0.001, digits = 3, expected = "-0.001"),
            TestCase(number = -0.001, digits = 2, expected = "0.00"),
            TestCase(number = 0.001, digits = 2, expected = "0.00"),
            TestCase(number = -0.005, digits = 2, expected = "-0.01"),
            TestCase(number = -0.0051, digits = 2, expected = "-0.01"),
            TestCase(number = 1123345.123, digits = 2, expected = "1123345.12"),
        )

        testCases.forEach { testCase ->
            val formatted = formatter.decimalLocaleAgnostic(number = testCase.number, digits = testCase.digits)
            assert(formatted == testCase.expected) { "Test case: $testCase, formatted: $formatted" }
        }
    }

    @Test
    fun testDecimalLocaleAgnostic_DoubleSize() {
        val formatter = DydxFormatter()
        data class TestCase(
            val number: Double,
            val size: Double?,
            val expected: String,
        )

        val testCases = listOf(
            TestCase(number = 1.0, size = 0.01, expected = "1.00"),
            TestCase(number = -0.001, size = 0.0, expected = "-0.001"), // invalid size
            TestCase(number = -0.001, size = 0.001, expected = "-0.001"),
            TestCase(number = -0.001, size = 0.01, expected = "0.00"),
            TestCase(number = 0.001, size = 0.01, expected = "0.00"),
            TestCase(number = -0.005, size = 0.01, expected = "-0.01"),
            TestCase(number = -0.0051, size = 0.01, expected = "-0.01"),
            TestCase(number = 1.6, size = 1.0, expected = "2"),
            TestCase(number = 1123345.123, size = 0.01, expected = "1123345.12"),
            TestCase(number = 1123345.126, size = 0.01, expected = "1123345.13"),
            TestCase(number = 1123349.123, size = 10.0, expected = "1123350"),
            TestCase(number = 1123341.123, size = 10.0, expected = "1123340"),
            TestCase(number = -1123341.123, size = 10.0, expected = "-1123340"),
            TestCase(number = 1123341.123, size = 100000000.0, expected = "0"),
            TestCase(number = 1123341.123, size = null, expected = "1123341.123"),
        )

        testCases.forEach { testCase ->
            val formatted = formatter.decimalLocaleAgnostic(number = testCase.number, size = testCase.size)
            assert(formatted == testCase.expected) { "Test case: $testCase, formatted: $formatted" }
        }
    }

    @Test
    fun testRaw() {
        val formatter = DydxFormatter()
        data class TestCase(
            val number: Double,
            val digits: Int?,
            val expected: String,
            var locale: Locale = Locale.getDefault()
        )

        val testCases = listOf(
            TestCase(number = 1.0, digits = 2, expected = "1.00"),
            TestCase(number = -0.001, digits = 0, expected = "0"),
            TestCase(number = -0.001, digits = 3, expected = "-0.001"),
            TestCase(number = -0.001, digits = 2, expected = "0.00"),
            TestCase(number = 0.001, digits = 2, expected = "0.00"),
            TestCase(number = -0.005, digits = 2, expected = "-0.01"),
            TestCase(number = -0.0051, digits = 2, expected = "-0.01"),
            TestCase(number = 1.0, digits = null, expected = "1.0"),
            TestCase(number = 1123345.123, digits = 2, expected = "1123345.12"),
            TestCase(number = 1123345.123, digits = 2, expected = "1123345,12", locale = Locale.FRANCE),
        )

        testCases.forEach { testCase ->
            val formatted = formatter.raw(number = testCase.number, digits = testCase.digits, locale = testCase.locale)
            assert(formatted == testCase.expected) { "Test case: $testCase, formatted: $formatted" }
        }
    }

    @Test
    fun testRaw_DoubleSize() {
        val formatter = DydxFormatter()

        data class TestCase(
            val number: Double,
            val size: Double?,
            val expected: String,
            var locale: Locale = Locale.getDefault()
        )

        val testCases = listOf(
            TestCase(number = 1.0, size = 0.01, expected = "1.00"),
            TestCase(number = -0.001, size = 0.0, expected = "-0.001"), // invalid size
            TestCase(number = -0.001, size = 0.001, expected = "-0.001"),
            TestCase(number = -0.001, size = 0.01, expected = "0.00"),
            TestCase(number = 0.001, size = 0.01, expected = "0.00"),
            TestCase(number = -0.005, size = 0.01, expected = "-0.01"),
            TestCase(number = -0.0051, size = 0.01, expected = "-0.01"),
            TestCase(number = 1.0, size = null, expected = "1.0"),
            TestCase(number = 1123345.123, size = 0.01, expected = "1123345.12"),
            TestCase(number = 1123345.123, size = 0.01, expected = "1123345,12", locale = Locale.FRANCE),
            TestCase(number = 1123349.123, size = 10.0, expected = "1123350", locale = Locale.FRANCE),
            TestCase(number = 1123344.123, size = 10.0, expected = "1123340", locale = Locale.FRANCE),
            TestCase(number = 1.0, size = null, expected = "1.0", locale = Locale.FRANCE),
            TestCase(number = 1123341.123, size = 100000000.0, expected = "0", locale = Locale.FRANCE),
        )

        testCases.forEach { testCase ->
            val formatted = formatter.raw(number = testCase.number, size = testCase.size, locale = testCase.locale)
            assert(formatted == testCase.expected) { "Test case: $testCase, formatted: $formatted" }
        }
    }
}
