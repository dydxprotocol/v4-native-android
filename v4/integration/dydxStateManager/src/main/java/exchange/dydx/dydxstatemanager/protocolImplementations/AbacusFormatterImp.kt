package exchange.dydx.dydxstatemanager.protocolImplementations

import exchange.dydx.abacus.protocols.FormatterProtocol
import exchange.dydx.trading.common.formatter.DydxFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbacusFormatterImp @Inject constructor(
    private val formatter: DydxFormatter
) : FormatterProtocol {
    override fun dollar(value: Double?, tickSize: String?): String? {
        if (value == null) {
            return null
        }

        val digits = tickSize?.let { digits(it) } ?: 2

        return formatter.dollar(value, digits)
    }

    override fun percent(value: Double?, digits: Int): String? {
        return formatter.percent(value, digits)
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
