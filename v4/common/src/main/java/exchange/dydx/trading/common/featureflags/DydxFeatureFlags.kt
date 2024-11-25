package exchange.dydx.trading.common.featureflags

import exchange.dydx.utilities.utils.SharedPreferencesStore
import javax.inject.Inject

enum class DydxFeatureFlag {
    deployment_url,
    force_mainnet,
    abacus_static_typing,
    vault_enabled,
    metadata_service,
}

class DydxFeatureFlags @Inject constructor(
    private val sharedPreferences: SharedPreferencesStore
) {
    fun isFeatureEnabled(featureFlag: DydxFeatureFlag, default: Boolean = false): Boolean {
        val value = sharedPreferences.read(featureFlag.name)
        if (value != null) {
            return value.toBoolean() || value == "1"
        }
        return default
    }

    fun valueForFeature(featureFlag: DydxFeatureFlag): String? {
        return sharedPreferences.read(featureFlag.name)
    }
}
