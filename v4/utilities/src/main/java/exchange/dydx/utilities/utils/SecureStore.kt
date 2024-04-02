package exchange.dydx.utilities.utils

import android.app.Application
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStore @Inject constructor(
    application: Application,
) : StoreProtocol {
    companion object {
        private const val PREFERENCES_NAME = "SecureStorePrefs"
        private const val DEFAULT_STRING_VALUE = ""
        private const val DEFAULT_BOOLEAN_VALUE = false
    }

    private val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        PREFERENCES_NAME,
        masterKeyAlias,
        application,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private var _stateUpdatedCount = MutableStateFlow(0)

    override val stateUpdatedCount: StateFlow<Int>
        get() = _stateUpdatedCount

    override fun save(data: String, key: String) {
        sharedPreferences.edit().putString(key, data).apply()
    }

    override fun read(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun read(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    override fun delete(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}
