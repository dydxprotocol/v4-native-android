package exchange.dydx.trading.common

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import exchange.dydx.utilities.utils.DebugEnabled
import exchange.dydx.utilities.utils.Logging
import exchange.dydx.utilities.utils.SharedPreferencesStore

interface AppConfig {
    val appContext: Context?
    val appVersionName: String
    val appVersionCode: String
    val debug: Boolean
    val activityClass: Class<*>?
    val appScheme: String?
        get() = appContext?.getString(R.string.app_scheme)
    val appSchemeHost: String?
        get() = appContext?.getString(R.string.app_scheme_host)
    val appWebHost: String?

    companion object {
        const val ANDROID_LOGGING = Log.INFO
        const val ABACUS_LOGGING: Int = Log.INFO - 2
        const val WOOD_LOGGING = Log.DEBUG
        const val VERBOSE_LOGGING = true

        val Preview: AppConfig = AppConfigImpl(
            appContext = null,
            appVersionName = "v0-preview",
            appVersionCode = "0",
            debug = BuildConfig.DEBUG,
            activityClass = null,
            preferencesStore = null,
        )
    }
}

data class AppConfigImpl(
    override val appContext: Application?,
    override val appVersionName: String,
    override val appVersionCode: String,
    override val debug: Boolean,
    override val activityClass: Class<*>?,
    private val preferencesStore: SharedPreferencesStore?,
    private val logger: Logging? = null,
) : AppConfig {
    override val appWebHost: String?
        get() {
            if (appContext == null || preferencesStore == null) {
                logger?.e("AppConfigImpl", "appContext or preferencesStore is null")
                return null
            }

            val appDeployment = appContext.getString(R.string.app_deployment)
            return if (appDeployment == "MAINNET" && DebugEnabled.enabled(preferencesStore)) {
                // Force to public testnet host if user has enabled debug mode, otherwise US test
                // users will be blocked from accessing the asset images/descriptions, which are fetched
                // based on app_web_host.
                "v4.testnet.dydx.exchange"
            } else {
                appContext.getString(R.string.app_web_host)
            }
        }
}

@Composable
fun PreviewAppConfig(): AppConfig {
    if (!LocalInspectionMode.current) {
        throw DydxException("This object is designed for Preview scope only")
    }
    return AppConfig.Preview
}
