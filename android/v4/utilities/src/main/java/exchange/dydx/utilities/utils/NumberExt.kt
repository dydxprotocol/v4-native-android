package exchange.dydx.utilities.utils

import android.content.res.Resources
import androidx.compose.ui.unit.Dp

enum class NumericFilter {
    NotNegative,
}

fun Double.filter(numericFilter: NumericFilter): Double? {
    return when (numericFilter) {
        NumericFilter.NotNegative -> if (this >= 0) this else null
    }
}

val Int.toPx
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Float.toPx
    get() = this * Resources.getSystem().displayMetrics.density

val Int.toDp: Dp
    get() = Dp((this / Resources.getSystem().displayMetrics.density))

val Float.toDp: Dp
    get() = Dp((this / Resources.getSystem().displayMetrics.density))
