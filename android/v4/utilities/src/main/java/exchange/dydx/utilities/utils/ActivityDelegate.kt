package exchange.dydx.utilities.utils

import android.app.Activity

/**
 * General activity delegate that allows accessing activity from Hilt components higher than
 * ActivityComponent.
 *
 * Feel free to move this somewhere more general if you need to use it for other stuff.
 */
interface ActivityDelegate {
    var activity: Activity?

    fun takeActivity(activity: Activity) {
        this.activity = activity
    }

    fun dropActivity(activity: Activity) {
        // outgoing activity can be destroyed after incoming activity has already been created
        if (activity == this.activity) {
            this.activity = null
        }
    }
}
