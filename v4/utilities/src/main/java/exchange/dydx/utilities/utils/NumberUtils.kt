package exchange.dydx.utilities.utils

fun Double.rounded(toPlaces: Int): Double {
    if (toPlaces < 0) throw IllegalArgumentException()
    val factor = Math.pow(10.0, toPlaces.toDouble())
    return Math.round(this * factor) / factor
}

fun Float.rounded(toPlaces: Int): Float {
    return this.toDouble().rounded(toPlaces).toFloat()
}
