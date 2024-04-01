package exchange.dydx.trading

import android.app.Application
import com.facebook.stetho.Stetho
import dagger.hilt.android.HiltAndroidApp
import exchange.dydx.platformui.designSystem.theme.ThemeSettings
import exchange.dydx.trading.common.logger.DydxLogger
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class DydxApplication : Application() {

    @Inject lateinit var logger: DydxLogger

    @Inject lateinit var themeSettings: ThemeSettings

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(
                logger.debugTree,
                logger.woodTree(this),
            )
            Stetho.initializeWithDefaults(this)
        }
    }
}
