package exchange.dydx.trading

import android.app.Application
import android.os.StrictMode
import com.facebook.stetho.Stetho
import dagger.hilt.android.HiltAndroidApp
import exchange.dydx.abacus.jvm.AbacusAndroid
import exchange.dydx.platformui.designSystem.theme.ThemeSettings
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.logger.DydxLogger
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class DydxApplication : Application() {

    // Do not remove - this is used to trigger initialization via Dagger
    // This is an anti-pattern, do not copy.
    @Inject lateinit var themeSettings: ThemeSettings

    @Inject lateinit var logger: DydxLogger

    override fun onCreate() {
        super.onCreate()

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
            if (AppConfig.ABACUS_LOGGING >= 0) {
                AbacusAndroid.enableDebug("dydx#AbacusCore", AppConfig.ABACUS_LOGGING)
            }
        }
    }
}
