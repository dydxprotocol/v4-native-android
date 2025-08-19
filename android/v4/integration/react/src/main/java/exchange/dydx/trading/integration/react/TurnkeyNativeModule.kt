package exchange.dydx.trading.integration.react

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule

@ReactModule(name = TurnkeyNativeModule.NAME)
internal class TurnkeyNativeModule(
    private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext), LifecycleEventListener {
    companion object Companion {
        private const val NAME = "TurnkeyNativeModule"
    }

    private val pendingCallbacks = mutableMapOf<String, (String) -> Unit>()

    override fun getName(): String = TurnkeyNativeModule.NAME

    var delegate: TurnkeyBridgeManagerDelegate? = null

    init {
        reactContext.addLifecycleEventListener(this)
    }

    fun emailTokenReceived(token: String) {
        if (reactContext.hasActiveReactInstance()) {
            val params = Arguments.createMap()
            params.putString("token", token)

            val jsModule = reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            jsModule?.emit(eventName = "EmailTokenReceived", data = params)
        } else {
            print("React Native instance is not active.")
        }
    }

    fun uploadDydxAddress(dydxAddress: String, callback: (String) -> Unit) {
        if (reactContext.hasActiveReactInstance()) {
            pendingCallbacks[dydxAddress] = callback

            val params = Arguments.createMap()
            params.putString("dydxAddress", dydxAddress)

            val jsModule = reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            jsModule?.emit(eventName = "DydxAddressReceived", data = params)
        } else {
            print("React Native instance is not active.")
        }
    }

    fun requestJsFunction(callbackId: String, callback: (String) -> Unit) {
        pendingCallbacks[callbackId] = callback

        val params = Arguments.createMap()
        params.putString("callbackId", callbackId)

        if (reactContext.hasActiveReactInstance()) {
            val jsModule = reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            jsModule?.emit("NativeToJsRequest", params)
        } else {
            print("React Native instance is not active.")
        }
    }

    @ReactMethod
    fun onJsResponse(callbackId: String, result: String) {
        pendingCallbacks[callbackId]?.invoke(result)
        pendingCallbacks.remove(callbackId)
    }

    @ReactMethod
    fun onAuthRouteToWallet() {
        delegate?.onAuthRouteToWallet()
    }

    @ReactMethod
    fun onAuthRouteToDesktopQR() {
        delegate?.onAuthRouteToDesktopQR()
    }

    @ReactMethod
    fun onAuthCompleted(
        onboardingSignature: String,
        evmAddress: String,
        svmAddress: String,
        mnemonics: String,
        loginMethod: String,
        userEmail: String?
    ) {
        delegate?.onAuthCompleted(
            onboardingSignature,
            evmAddress,
            svmAddress,
            mnemonics,
            loginMethod,
            userEmail,
        )
    }

    override fun onHostDestroy() {
        print("Host is being destroyed, cleaning up resources.")
    }

    override fun onHostPause() {
        print("Host is paused, saving state if necessary.")
    }

    override fun onHostResume() {
        print("Host is resumed, ready to handle events.")
    }
}
