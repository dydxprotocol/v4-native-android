package exchange.dydx.platformui.components.charts.formatter

import com.github.mikephil.charting.formatter.ValueFormatter

open class ValueAxisFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return value.toString()
    }
}
