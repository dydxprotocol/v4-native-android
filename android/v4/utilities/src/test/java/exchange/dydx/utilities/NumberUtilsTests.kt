package exchange.dydx.utilities

import exchange.dydx.utilities.utils.rounded
import junit.framework.TestCase.assertEquals
import org.junit.Test

class NumberUtilsTests {
    @Test
    fun testRounded() {
        assertEquals(1.0, 1.0.rounded(0))
        assertEquals(1.0, 1.1.rounded(0))
        assertEquals(2.0, 1.5.rounded(0))
        assertEquals(1.1, 1.1.rounded(1))
        assertEquals(1.12, 1.123.rounded(2))
        assertEquals(1.14, 1.138.rounded(2))
        assertEquals(1.123, 1.123.rounded(3))
        assertEquals(-1.12, (-1.123).rounded(2))
        assertEquals(-1.13, (-1.129).rounded(2))
        try {
            1.0.rounded(-1)
            assert(false)
        } catch (e: IllegalArgumentException) {
            assert(true)
        }
    }
}
