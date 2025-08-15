package exchange.dydx.trading.integration.react

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.facebook.react.ReactApplication
import com.facebook.react.ReactRootView
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler

@Composable
fun ReactNativeView(
    moduleName: String,                 // matches AppRegistry.registerComponent(...)
    initialProps: Bundle? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Get the shared ReactInstanceManager from the Application's ReactNativeHost
    val reactInstanceManager = remember {
        val app = context.applicationContext as ReactApplication
        app.reactNativeHost.reactInstanceManager
    }

    // Keep one ReactRootView instance
    val reactRootView = remember { ReactRootView(context) }

    AndroidView(
        factory = {
            reactRootView.apply {
                // Starting the RN app inside this view
                startReactApplication(reactInstanceManager, moduleName, initialProps)
            }
        },
        modifier = modifier
    )

    // Forward lifecycle
    DisposableEffect(lifecycleOwner, activity) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME ->
                    reactInstanceManager.onHostResume(
                        activity,
                        object : DefaultHardwareBackBtnHandler {
                            override fun invokeDefaultOnBackPressed() {
                                activity.onBackPressed() // .onBackPressedDispatcher.onBackPressed()
                            }
                        })

                Lifecycle.Event.ON_PAUSE -> reactInstanceManager.onHostPause(activity)
                Lifecycle.Event.ON_DESTROY -> reactInstanceManager.onHostDestroy(activity)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            reactRootView.unmountReactApplication()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

// Helper to find the Activity from a Context
private tailrec fun Context.findActivity(): Activity {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> error("No Activity in context chain")
    }
}
