package exchange.dydx.trading.common

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode

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
        get() = appContext?.getString(R.string.app_web_host)

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
        )
    }
}

data class AppConfigImpl(
    override val appContext: Context?,
    override val appVersionName: String,
    override val appVersionCode: String,
    override val debug: Boolean,
    override val activityClass: Class<*>?,
) : AppConfig

@Composable
fun PreviewAppConfig(): AppConfig {
    if (!LocalInspectionMode.current) {
        throw DydxException("This object is designed for Preview scope only")
    }
    return AppConfig.Preview
}
