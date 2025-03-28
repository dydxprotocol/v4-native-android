package exchange.dydx.trading.common.featureflags

import exchange.dydx.utilities.utils.SharedPreferencesStore
import javax.inject.Inject

enum class DydxFeatureFlag {
    deployment_url,
    force_mainnet,
    abacus_static_typing,
    vault_enabled,
    skip_go_fast;

    val defaultValue: Boolean
        get()  {
            return when (this) {
                deployment_url -> false
                force_mainnet -> false
                abacus_static_typing -> true
                vault_enabled -> true
                skip_go_fast -> true
            }
        }
}

class DydxFeatureFlags @Inject constructor(
    private val sharedPreferences: SharedPreferencesStore
) {
    fun isFeatureEnabled(featureFlag: DydxFeatureFlag): Boolean {
        val value = sharedPreferences.read(featureFlag.name)
        if (value != null) {
            return value.toBoolean() || value == "1"
        }
        return featureFlag.defaultValue
    }

    fun valueForFeature(featureFlag: DydxFeatureFlag): String? {
        return sharedPreferences.read(featureFlag.name)
    }
}
