package exchange.dydx.trading

import android.app.Application
import android.os.StrictMode
import com.facebook.stetho.Stetho
import dagger.hilt.android.HiltAndroidApp
import exchange.dydx.platformui.designSystem.theme.ThemeSettings
import exchange.dydx.trading.common.logger.DydxLogger
import timber.log.Timber
import javax.inject.Inject

import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.load
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.soloader.SoLoader
//import com.facebook.react.soloader.OpenSourceMergedSoMapping

@HiltAndroidApp
class DydxApplication : Application(), ReactApplication {

    override val reactNativeHost: ReactNativeHost =
        object : DefaultReactNativeHost(this) {
            override fun getPackages(): List<ReactPackage> = PackageList(this).packages
            override fun getJSMainModuleName(): String = "index"
            override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG
            override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
            override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED

            override fun getJSBundleFile(): String? {
                // Example: load from local assets (release mode)
                return if (BuildConfig.DEBUG) {
                    "http://192.168.1.7:8081/index.bundle?platform=android"  // Will fallback to Metro
                } else {
                    "assets://index.android.bundle"
                }
            }
        }

    override val reactHost: ReactHost
        get() = getDefaultReactHost(applicationContext, reactNativeHost)

    // Do not remove - this is used to trigger initialization via Dagger
    // This is an anti-pattern, do not copy.
    @Inject
    lateinit var themeSettings: ThemeSettings

    @Inject
    lateinit var logger: DydxLogger

    override fun onCreate() {
        super.onCreate()

        //SoLoader.init(this, OpenSourceMergedSoMapping)
        SoLoader.init(this, false)
        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
            load()
        }

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build(),
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build(),
            )

            Timber.plant(
                logger.debugTree,
                logger.woodTree(this),
            )
            Stetho.initializeWithDefaults(this)
        }
    }
}
