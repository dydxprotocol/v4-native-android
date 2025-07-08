package exchange.dydx.utilities

import exchange.dydx.utilities.utils.jsonStringToRawStringMap
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class JsonUtilsTests {

    @Test fun jsonStringToRawStringMap() {
        val json =
            """
                {
                  "type": "MARKET",
                  "size": 10.0,
                  "nested" : {
                    "someValue": "SOME-VALUE",
                    "anotherValue": 25.0
                  }
                }
            """.trimIndent()

        val mapped = json.jsonStringToRawStringMap()!!

        assertEquals(mapped["type"], "MARKET")

        assert(mapped["size"] is JsonPrimitive)
        assertEquals(mapped["size"].toString(), "10.0")

        assertEquals((mapped["nested"] as Map<String, Any>)["someValue"], "SOME-VALUE")

        assert((mapped["nested"] as Map<String, Any>)["anotherValue"] is JsonPrimitive)
        assertEquals((mapped["nested"] as Map<String, Any>)["anotherValue"].toString(), "25.0")
    }
}

private fun assertEquals(actual: Any?, expected: Any?) {
    assert(actual == expected) { "$actual != $expected" }
}
