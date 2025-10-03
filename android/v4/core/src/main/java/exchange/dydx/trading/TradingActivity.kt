package exchange.dydx.trading

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.facebook.react.ReactApplication
import com.facebook.react.ReactInstanceManager
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler
import dagger.hilt.android.AndroidEntryPoint
import exchange.dydx.dydxCartera.CarteraConfig
import exchange.dydx.dydxfiatramp.DydxMoonPayRamp
import exchange.dydx.dydxstatemanager.AbacusStateManager
import exchange.dydx.integration.javascript.JavascriptRunnerWebview
import exchange.dydx.platformui.components.container.PlatformInfoContainer
import exchange.dydx.platformui.designSystem.theme.ThemeConfig
import exchange.dydx.platformui.designSystem.theme.ThemeSettings
import exchange.dydx.platformui.designSystem.theme.colorMap
import exchange.dydx.trading.common.logger.DydxLogger
import exchange.dydx.trading.core.AnalyticsSetup
import exchange.dydx.trading.core.CarteraSetup
import exchange.dydx.trading.core.CoreViewModel
import exchange.dydx.trading.core.DydxNavGraph
import exchange.dydx.trading.core.biometric.DydxBiometricPrompt
import exchange.dydx.trading.core.biometric.DydxBiometricView
import exchange.dydx.trading.feature.shared.PreferenceKeys
import exchange.dydx.trading.feature.shared.analytics.AnalyticsEvent
import exchange.dydx.trading.integration.fcm.PushPermissionRequesterProtocol
import exchange.dydx.trading.integration.react.TurnkeyReactBridge
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TradingActivity"

/**
 * Main activity for Dydx Trading
 */

@AndroidEntryPoint
class TradingActivity : FragmentActivity(), DefaultHardwareBackBtnHandler {

    // This is the main ViewModel that the Activity will use to communicate with Compose-scoped code.
    private val viewModel: CoreViewModel by viewModels()

    @Inject lateinit var preferencesStore: SharedPreferencesStore

    @Inject lateinit var abacusStateManager: AbacusStateManager

    @Inject lateinit var pushPermissionRequester: PushPermissionRequesterProtocol

    @Inject lateinit var turnkeyReactBridge: TurnkeyReactBridge

    @Inject lateinit var moonPayRamp: DydxMoonPayRamp

    private lateinit var reactInstanceManager: ReactInstanceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // if (BuildConfig.DEBUG) Debug.waitForDebugger()   // pause here
        super.onCreate(savedInstanceState)

        setUpReactNativeBridge()

        moonPayRamp.takeActivity(this)
        pushPermissionRequester.takeActivity(this)
        viewModel.logger.d(TAG, "TradingActivity#onCreate")

        val action: String? = intent?.action
        val data: Uri? = intent?.data
        if (action == "android.intent.action.VIEW" && data != null) {
            CarteraConfig.handleResponse(data)
        }

        CarteraSetup.run(this, viewModel.logger, abacusStateManager)
        AnalyticsSetup.run(viewModel.compositeTracking, this, viewModel.logger)

        viewModel.compositeTracking.log(
            event = AnalyticsEvent.APP_START.rawValue,
            data = null,
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)

        lifecycleScope.launch {
            // Coroutine / Flow based alternative to overriding onResume()
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                // This pattern allows flow based execution on the activity object from the
                // ViewModel without the ViewModel having to hold a reference to the Activity.
                // Each new activity will resubscribe to these flows on resume.
            }
        }

        setContentWithJS {
            BiometricPrompt()
        }

        // The first time an activity is launched, the intent comes here.
        // If an intent is launched to an already running activity, it comes to
        // `onNewIntent` instead. Route both to same place for now.
        viewModel.router.handleIntent(intent)

        // Start the workers: Note the CarteraSetupWorker must start here because
        // the WalletConnect expects the SDK initialization to happen at Activity.onCreate()
        viewModel.startWorkers()
    }

    override fun invokeDefaultOnBackPressed() {
        super.onBackPressed()
    }

    private fun setUpReactNativeBridge() {
        val reactNativeHost = (application as ReactApplication).reactNativeHost
        reactInstanceManager = reactNativeHost.reactInstanceManager

        reactInstanceManager.addReactInstanceEventListener(
            object : com.facebook.react.ReactInstanceEventListener {
                override fun onReactContextInitialized(context: com.facebook.react.bridge.ReactContext) {
                    turnkeyReactBridge.updateContext(context)
                }
            },
        )
        if (reactInstanceManager.hasStartedCreatingInitialContext() == false) {
            reactInstanceManager.createReactContextInBackground()
        }
    }

    override fun onPause() {
        super.onPause()
        abacusStateManager.setReadyToConnect(false)
    }

    override fun onResume() {
        super.onResume()
        abacusStateManager.setReadyToConnect(true)

        // Helps ensure ReactRootView can handle keyboard
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onDestroy() {
        super.onDestroy()
        pushPermissionRequester.dropActivity(this)
    }

    private fun setContentWithJS(
        content: @Composable () -> Unit,
    ) {
        setContent {
//            FragmentInCompose(
//                fragmentManager = supportFragmentManager,
//                fragment = TurnkeyReactBridge.reactNativeFragment,
//            )

            JavascriptRunnerWebview(
                modifier = Modifier,
                isVisible = false,
                javascriptRunner = viewModel.cosmosClient.runner,
                logger = viewModel.logger,
            )

            content()
        }
    }

    @Composable
    private fun FragmentInCompose(
        fragmentManager: FragmentManager,
        fragment: Fragment,
        containerId: Int = View.generateViewId()
    ) {
        AndroidView(
            factory = { context ->
                FragmentContainerView(context).apply {
                    id = containerId
                }
            },
            update = { view ->
                val existingFragment = fragmentManager.findFragmentById(view.id)
                if (existingFragment == null) {
                    fragmentManager
                        .beginTransaction()
                        .replace(view.id, fragment)
                        .commit()
                }
            },
        )
    }

    @Composable
    private fun BiometricPrompt() {
        DydxBiometricPrompt.Content(
            activity = this,
            logger = viewModel.logger,
            processSuccess = { result, error ->
                setContentWithJS {
                    if (result) {
                        MainContent()
                    } else {
                        BiometricErrorContent(error)
                    }
                }
            },
        )
    }

    @Composable
    private fun BiometricErrorContent(error: String?) {
        DydxBiometricView.Content(
            modifier = Modifier,
            error = error,
            retryAction = {
                setContentWithJS {
                    BiometricPrompt()
                }
            },
        )
    }

    @Composable
    private fun MainContent() {
        key(themeChangedState) {
            Box {
                DydxNavGraph(
                    appRouter = viewModel.router,
                    logger = viewModel.logger,
                )

                PlatformInfoContainer()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // must store the new intent unless getIntent()
        // will return the old one
        setIntent(intent)

        val action: String? = intent.action
        val data: Uri? = intent.data
        if (action == "android.intent.action.VIEW" && data != null) {
            CarteraConfig.handleResponse(data)
        }

        // Notify the React Native instance manager of the new intent
        // This is necessary for React Native module to handle deep links correctly.
        reactInstanceManager.onNewIntent(intent)

        viewModel.router.handleIntent(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            DydxLogger.DATABASE_EXPORT_CODE -> {
                viewModel.loggerDeprecated.shareDb(this, data)
            }

            else ->
                viewModel.logger.e(TAG, "onActivityResult: unknown request code: $requestCode")
        }
    }

    // This is a state that is used to force a recomposition when the theme changes.
    private var themeChangedState by mutableIntStateOf(0)

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val theme = preferencesStore.read(key = PreferenceKeys.Theme)
        if (theme == "system") {
            when (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    // Night mode is not active, we're using the light theme
                    if (ThemeSettings.shared.themeConfig.value != ThemeConfig.light(this)) {
                        ThemeSettings.shared.themeConfig.value = ThemeConfig.light(this)
                        ThemeSettings.shared.colorMap = mapOf()
                        themeChangedState++
                    }
                }

                Configuration.UI_MODE_NIGHT_YES -> {
                    // Night mode is active, we're using dark theme
                    if (ThemeSettings.shared.themeConfig.value != ThemeConfig.dark(this)) {
                        ThemeSettings.shared.themeConfig.value = ThemeConfig.dark(this)
                        ThemeSettings.shared.colorMap = mapOf()
                        themeChangedState++
                    }
                }
            }
        }
    }
}
