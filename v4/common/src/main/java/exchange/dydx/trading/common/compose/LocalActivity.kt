package exchange.dydx.trading.common.compose

import android.app.Activity
import androidx.compose.runtime.staticCompositionLocalOf

val LocalActivity = staticCompositionLocalOf<Activity> {
    error("LocalActivity not present")
}
