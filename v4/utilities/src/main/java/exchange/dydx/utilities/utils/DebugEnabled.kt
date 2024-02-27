package exchange.dydx.utilities.utils

import exchange.dydx.utilities.BuildConfig

object DebugEnabled {
    const val key = "debug.enabled"

    fun enabled(preferencesStore: SharedPreferencesStore): Boolean {
        if (BuildConfig.DEBUG) {
            return true
        }

        return preferencesStore.read(key).toBoolean()
    }

    fun update(preferencesStore: SharedPreferencesStore, enabled: Boolean) {
        preferencesStore.save(enabled.toString(), key)
    }
}
