package exchange.dydx.trading.integration.statsig

import android.R.attr.name
import com.statsig.androidsdk.EvaluationReason
import com.statsig.androidsdk.Statsig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import exchange.dydx.trading.common.featureflags.RemoteFlags

object StatsigFlagsImpl : RemoteFlags {

    // Cache first accessed value to ensure single value through lifetime of app.
    private val firstAccessCache = mutableMapOf<String, Boolean>()

    override fun isEnabled(name: String, default: Boolean): Boolean {
        return firstAccessCache[name] ?: run {
            val (gateReason, gateValue) = try {
                val gate = Statsig.getFeatureGate(name)
                gate.getEvaluationDetails().reason to gate.getValue()
            } catch (e: IllegalStateException) {
                // Catch uninitialized SDK error.
                if (BuildConfig.DEBUG) {
                    throw e
                } else {
                    EvaluationReason.Uninitialized to default
                }
            }

            val flagValue = when (gateReason) {
                EvaluationReason.Network,
                EvaluationReason.NetworkNotModified,
                EvaluationReason.Cache,
                EvaluationReason.Sticky,
                EvaluationReason.LocalOverride,
                EvaluationReason.Bootstrap -> gateValue

                EvaluationReason.Unrecognized,
                EvaluationReason.InvalidBootstrap,
                EvaluationReason.Error -> default

                EvaluationReason.Uninitialized -> if (BuildConfig.DEBUG) {
                    //  error("Statsig SDK not initialized")
                    default
                } else {
                    default
                }
            }
            firstAccessCache[name] = flagValue
            flagValue
        }
    }

    override fun <T> getParamStoreValue(key: String, default: T): T {
        val store = Statsig.getParameterStore("v4_params")
        if (default is String) {
            return (store.getString(key, default as String) ?: default) as T
        } else if (default is Boolean) {
            return store.getBoolean(key, default as Boolean) as T
        } else if (default is Double) {
            return store.getDouble(key, default as Double) as T
        } else if (default is Array<*>) {
            return (store.getArray(key, default as Array<*>) ?: default) as T
        }
        return default
    }
}

@InstallIn(SingletonComponent::class)
@Module
object StatsigModule {
    @Provides fun bindStatsigFlags(): RemoteFlags = StatsigFlagsImpl
}
