package exchange.dydx.trading.integration.react

import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint
import com.facebook.react.defaults.DefaultReactActivityDelegate

//
// Sample stand-alone React Native activity for testing purposes.
//
class MyReactActivity : ReactActivity() {

    override fun getMainComponentName(): String = "HelloWorld"

    override fun createReactActivityDelegate(): ReactActivityDelegate =
        DefaultReactActivityDelegate(
            activity = this,
            mainComponentName = mainComponentName,
            fabricEnabled = DefaultNewArchitectureEntryPoint.fabricEnabled,
        )
}
