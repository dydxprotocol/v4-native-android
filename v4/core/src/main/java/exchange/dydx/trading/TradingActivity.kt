package exchange.dydx.trading

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch
import timber.log.Timber

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.tag(TAG).i("TradingActivity#onCreate")

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
        PlatformInfoScaffold(
            modifier = Modifier,
            platformInfo = viewModel.platformInfo,
        ) {
            DydxNavGraph(
                appRouter = viewModel.router,
                modifier = Modifier,
            )
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
                viewModel.logger.shareDb(this, data)
            }

            else ->
                Timber.tag(TAG).w("onActivityResult: unknown request code: %d", requestCode)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        when (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                // Night mode is not active, we're using the light theme
                ThemeSettings.shared.themeConfig.value = ThemeConfig.light(this)
                ThemeSettings.shared.colorMap = mapOf()
            }

            Configuration.UI_MODE_NIGHT_YES -> {
                // Night mode is active, we're using dark theme
                ThemeSettings.shared.themeConfig.value = ThemeConfig.dark(this)
                ThemeSettings.shared.colorMap = mapOf()
            }
        }
    }
}
