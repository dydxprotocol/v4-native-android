package exchange.dydx.trading.feature.market.marketinfo.components.funding

import exchange.dydx.platformui.components.charts.formatter.ValueAxisFormatter
import exchange.dydx.trading.common.formatter.DydxFormatter

class PercentAxisFormatter(private val formatter: DydxFormatter, private val digits: Int) :
    ValueAxisFormatter() {
    override fun getFormattedValue(value: Float): String {
        return formatter.percent(value.toDouble(), digits) ?: ""
    }
}
