package exchange.dydx.trading.common.featureflags

import exchange.dydx.utilities.utils.SharedPreferencesStore
import javax.inject.Inject

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
    vault_enabled,
    prompt_app_rating,
    skip_go_fast;

    val defaultValue: Boolean
        get() {
            return when (this) {
                force_mainnet -> false
                vault_enabled -> true
                prompt_app_rating -> false
                skip_go_fast -> true
            }
        }
}

class DydxFeatureFlags @Inject constructor(
    private val sharedPreferences: SharedPreferencesStore
) {
    fun isFeatureEnabled(featureFlag: DydxBoolFeatureFlag): Boolean {
        val value = sharedPreferences.read(featureFlag.name)
        if (value != null) {
            return value.toBoolean() || value == "1"
        }
        return featureFlag.defaultValue
    }

    fun stringForFeature(featureFlag: DydxStringFeatureFlag): String? {
        return sharedPreferences.read(featureFlag.name)
    }

    fun doubleForFeature(featureFlag: DydxDoubleFeatureFlag): Double {
        val value = sharedPreferences.read(featureFlag.name)
        return value?.toDoubleOrNull() ?: featureFlag.defaultValue
    }
}
