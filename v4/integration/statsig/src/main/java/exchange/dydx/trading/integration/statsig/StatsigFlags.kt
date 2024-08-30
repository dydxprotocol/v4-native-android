package exchange.dydx.trading.integration.statsig

import com.statsig.androidsdk.EvaluationReason
import com.statsig.androidsdk.Statsig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

interface StatsigFlags {
    fun isEnabled(name: String, default: Boolean = false): Boolean
}

object RealStatsigFlags : StatsigFlags {

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
                    error("Statsig SDK not initialized")
                } else {
                    default
                }
            }
            firstAccessCache[name] = flagValue
            flagValue
        }
    }
}

@InstallIn(SingletonComponent::class)
@Module
object StatsigModule {
    @Provides fun bindStatsigFlags(): StatsigFlags = RealStatsigFlags
}
