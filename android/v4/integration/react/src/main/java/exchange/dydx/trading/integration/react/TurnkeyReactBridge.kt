package exchange.dydx.trading.integration.react

import com.facebook.react.ReactFragment
import com.facebook.react.ReactPackage
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class TurnkeyReactBridge @Inject constructor(
    private val logger: Logging
) {
    companion object {
        const val jSMainModuleName = "index"
        val reactPackage: ReactPackage = TurnkeyReactPackage()

        val reactNativeFragment = ReactFragment.Builder()
            .setComponentName("TurnkeyReact") // e.g., "HelloWorld"
            .setLaunchOptions(null) // Optional: pass initial props to React Native
            .build()
    }

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private lateinit var context: com.facebook.react.bridge.ReactContext

    fun updateContext(context: com.facebook.react.bridge.ReactContext) {
        this.context = context
        _isInitialized.value = true
    }

    fun testFunction() {
        if (!isInitialized.value) {
            throw IllegalStateException("TurnkeyReactBridge is not initialized")
        }

        val turnkeyNativeModule = context.getNativeModule(TurnkeyNativeModule::class.java)

        turnkeyNativeModule?.requestJsFunction("req123") { result ->
            print("Received result from JS: $result")
        }
    }
}
