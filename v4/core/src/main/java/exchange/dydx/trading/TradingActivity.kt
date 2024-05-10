package exchange.dydx.trading

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import exchange.dydx.integration.javascript.JavascriptRunnerWebview
import exchange.dydx.platformui.components.PlatformInfoScaffold
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
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TradingActivity"

/**
 * Main activity for Dydx Trading
 */
val LocalTradingActivity = staticCompositionLocalOf<TradingActivity> {
    error("LocalTradingActivity not present")
}

@AndroidEntryPoint
class TradingActivity : FragmentActivity() {

    // This is the main ViewModel that the Activity will use to communicate with Compose-scoped code.
    private val viewModel: CoreViewModel by viewModels()

    @Inject
    lateinit var preferencesStore: SharedPreferencesStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.logger.d(TAG, "TradingActivity#onCreate")

        CarteraSetup.run(this)
        AnalyticsSetup.run(viewModel.compositeTracking, this)

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

    private fun setContentWithJS(
        content: @Composable () -> Unit,
    ) {
        setContent {
            viewModel.cosmosClient?.let {
                JavascriptRunnerWebview(
                    modifier = Modifier,
                    isVisible = false,
                    javascriptRunner = it.runner,
                )
            }
            content()
        }
    }

    @Composable
    private fun BiometricPrompt() {
        DydxBiometricPrompt.Content(
            activity = this,
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
            PlatformInfoScaffold(
                modifier = Modifier,
                platformInfo = viewModel.platformInfo,
            ) {
                DydxNavGraph(
                    appRouter = viewModel.router,
                    modifier = it,
                    logger = viewModel.logger,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            viewModel.router.handleIntent(intent)
        }
    }

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
