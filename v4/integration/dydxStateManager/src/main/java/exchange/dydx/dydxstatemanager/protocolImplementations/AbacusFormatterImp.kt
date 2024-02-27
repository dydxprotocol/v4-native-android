package exchange.dydx.dydxstatemanager.protocolImplementations

import exchange.dydx.abacus.protocols.FormatterProtocol
import java.text.DecimalFormat

class AbacusFormatterImp : FormatterProtocol {
    override fun dollar(value: Double?, tickSize: String?): String? {
        if (value == null) {
            return null
        }

        val digits = tickSize?.let { digits(it) } ?: 2

        val decimalFormat = if (value > 0) DecimalFormat("$#." + "#".repeat(digits)) else DecimalFormat("-$#." + "#".repeat(digits))
        return decimalFormat.format(value)
    }

    override fun percent(value: Double?, digits: Int): String? {
        if (value == null) {
            return null
        }
        val decimalFormat = DecimalFormat("$#." + "#".repeat(digits) + "%")
        return decimalFormat.format(value * 100)
    }

    private fun digits(size: String): Int {
        val components = size.split(".")
        if (components.size == 2) {
            return components.lastOrNull()?.length ?: 0
        } else {
            return ((components.firstOrNull()?.length ?: 1) - 1) * -1
        }
    }
}
