package exchange.dydx.trading.feature.market.marketinfo.components.prices

import exchange.dydx.platformui.components.charts.formatter.ValueAxisFormatter
import exchange.dydx.trading.common.formatter.DydxFormatter

class PriceAxisFormatter(
    private val formatter: DydxFormatter,
    private val tickSizeDecimals: Int
) : ValueAxisFormatter() {
    override fun getFormattedValue(value: Float): String {
        return formatter.dollar(value.toDouble(), tickSizeDecimals) ?: ""
    }
}
