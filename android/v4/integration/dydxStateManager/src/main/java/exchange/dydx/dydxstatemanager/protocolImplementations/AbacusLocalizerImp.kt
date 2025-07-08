package exchange.dydx.dydxstatemanager.protocolImplementations

import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.protocols.AbacusLocalizerProtocol
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.helper.DynamicLocalizer
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.utilities.utils.Logging
import exchange.dydx.utilities.utils.SharedPreferencesStore
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

// Temporary location, should probably make a separate dagger-qualifiers module.
@Qualifier annotation class LanguageKey

// While this class doesn't technically hold any state, it does have a side-effect on init.
// So, scoping this as a singleton.
@Singleton
class AbacusLocalizerImp @Inject constructor(
    private val sharedPreferencesStore: SharedPreferencesStore,
    @LanguageKey private val preferenceKey: String,
    private val ioImplementations: IOImplementations,
    private val logger: Logging,
) : AbacusLocalizerProtocol {

    private val TAG = "AbacusLocalizerImp"

    init {
        val language = sharedPreferencesStore.read(key = preferenceKey, defaultValue = "en")
        UIImplementationsExtensions.reset(language = language, ioImplementations)
        setLanguage(language = language) { successful, error ->
            if (successful) {
                logger.d(TAG, "Successfully set language to $language")
            } else {
                logger.e(TAG, "Failed to set language to $language: $error")
            }
        }
    }

    private val localizer: DynamicLocalizer?
        get() = UIImplementationsExtensions.shared?.localizer as? DynamicLocalizer

    override val languages: List<SelectionOption>
        get() {
            return localizer?.languages ?: emptyList()
        }

    override var language: String? = null
        get() {
            return localizer?.language
        }
        set(value) {
            if (field != value && value != null) {
                setLanguage(value) { successful, error ->
                    if (successful) {
                        field = value
                    }
                }
            }
        }

    override fun localize(path: String, paramsAsJson: String?): String {
        return UIImplementationsExtensions.shared?.localizer?.localize(path, paramsAsJson) ?: path
    }

    override fun setLanguage(language: String, callback: (successful: Boolean, error: ParsingError?) -> Unit) {
        if (language.contains("-")) {
            val languageParts = language.split("-")
            val language = languageParts[0]
            localizer?.setLanguage(language, callback)
        } else {
            localizer?.setLanguage(language, callback)
        }
    }
}
