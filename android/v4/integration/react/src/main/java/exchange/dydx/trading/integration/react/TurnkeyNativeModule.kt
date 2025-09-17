package exchange.dydx.trading.integration.react

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.util.UUID

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
    var trackingDelegate: TurnkeyTrackingDelegate? = null

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
        requestJsFunction(
            functionName = "DydxAddressReceived",
            params = mapOf("dydxAddress" to dydxAddress),
        ) { result ->
            callback(result)
        }
    }

    fun fetchDepositAddresses(dydxAddress: String, indexerUrl: String, callback: (String) -> Unit) {
        requestJsFunction(
            functionName = "FetchDepositAddresses",
            params = mapOf("dydxAddress" to dydxAddress, "indexerUrl" to indexerUrl),
        ) { result ->
            callback(result)
        }
    }

    fun requestJsFunction(functionName: String, params: Map<String, String> = emptyMap(), callback: (String) -> Unit) {
        val callbackId = UUID.randomUUID().toString()
        pendingCallbacks[callbackId] = callback

        val args = Arguments.createMap()
        args.putString("callbackId", callbackId)
        for ((key, value) in params) {
            args.putString(key, value)
        }

        if (reactContext.hasActiveReactInstance()) {
            val jsModule = reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            jsModule?.emit(functionName, args)
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
        userEmail: String?,
        dydxAddress: String?,
    ) {
        delegate?.onAuthCompleted(
            onboardingSignature,
            evmAddress,
            svmAddress,
            mnemonics,
            loginMethod,
            userEmail,
            dydxAddress,
        )
    }

    @ReactMethod
    fun onTrackingEvent(eventName: String, eventParams: ReadableMap) {
        val params: Map<String, String> = eventParams.toHashMap()
            .mapValues { it.value.toString() }
        trackingDelegate?.onTrackingEvent(eventName = eventName, eventParams = params)
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
