package exchange.dydx.trading

import android.app.Application
import android.os.StrictMode
import com.facebook.stetho.Stetho
import dagger.hilt.android.HiltAndroidApp
import exchange.dydx.abacus.jvm.AbacusAndroid
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.logger.DydxLogger
import timber.log.Timber

@HiltAndroidApp
class DydxApplication : Application() {

    private val logger = DydxLogger()

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
