package exchange.dydx.trading.feature.shared
import exchange.dydx.utilities.utils.SharedPreferencesStore

object NotificationEnabled {
    fun enabled(preferencesStore: SharedPreferencesStore): Boolean {
        return currentValue(preferencesStore) == "1"
    }

    fun currentValue(preferencesStore: SharedPreferencesStore): String {
        return preferencesStore.read(PreferenceKeys.Notifications, defaultValue = "1")
    }

    fun update(preferencesStore: SharedPreferencesStore, value: String) {
        preferencesStore.save(value, PreferenceKeys.Notifications)
    }
}
