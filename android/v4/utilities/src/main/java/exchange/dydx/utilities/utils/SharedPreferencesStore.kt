package exchange.dydx.utilities.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesStore @Inject constructor(
    application: Application,
) : StoreProtocol {
    companion object {
        private const val PREFERENCES_NAME = "SharedPreferences"
        private const val DEFAULT_STRING_VALUE = ""
        private const val DEFAULT_BOOLEAN_VALUE = false
    }

    private val sharedPreferences: SharedPreferences = application.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private var _stateUpdatedCount = MutableStateFlow(0)

    override val stateUpdatedCount: StateFlow<Int>
        get() = _stateUpdatedCount

    override fun save(data: String, key: String) {
        _stateUpdatedCount.value++
        sharedPreferences.edit().putString(key, data).apply()
    }

    override fun read(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun read(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    override fun delete(key: String) {
        _stateUpdatedCount.value++
        sharedPreferences.edit().remove(key).apply()
    }
}
