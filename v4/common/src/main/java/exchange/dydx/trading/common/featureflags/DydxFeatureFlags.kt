package exchange.dydx.trading.common.featureflags

import exchange.dydx.utilities.utils.SharedPreferencesStore
import javax.inject.Inject

interface RemoteFlags {
    fun isEnabled(name: String, default: Boolean = false): Boolean
}

enum class DydxStringFeatureFlag {
    deployment_url
}

enum class DydxDoubleFeatureFlag {
    min_usdc_for_deposit;

    val defaultValue: Double
        get() {
            return when (this) {
                min_usdc_for_deposit -> 10.0
            }
        }
}

enum class DydxBoolFeatureFlag {
    force_mainnet,
    ff_vault_enabled,
    ff_prompt_app_rating,
    ff_skip_go_fast;

    val defaultValue: Boolean
        get() {
            return when (this) {
                force_mainnet -> false
                ff_vault_enabled -> true
                ff_prompt_app_rating -> true
                ff_skip_go_fast -> true
            }
        }
}

class DydxFeatureFlags @Inject constructor(
    private val sharedPreferences: SharedPreferencesStore,
    private val remote: RemoteFlags
) {
    fun isFeatureEnabled(featureFlag: DydxBoolFeatureFlag): Boolean {
        val value = sharedPreferences.read(featureFlag.name)
        if (value != null) {
            return value.toBoolean() || value == "1"
        }
        return remote.isEnabled(name = featureFlag.name, default = featureFlag.defaultValue)
    }

    fun stringForFeature(featureFlag: DydxStringFeatureFlag): String? {
        return sharedPreferences.read(featureFlag.name)
    }

    fun doubleForFeature(featureFlag: DydxDoubleFeatureFlag): Double {
        val value = sharedPreferences.read(featureFlag.name)
        return value?.toDoubleOrNull() ?: featureFlag.defaultValue
    }
}
