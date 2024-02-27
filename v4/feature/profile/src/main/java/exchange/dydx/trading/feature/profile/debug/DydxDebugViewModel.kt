package exchange.dydx.trading.feature.profile.debug

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.settings.PlatformUISettings
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.views.SettingsView
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class DydxDebugViewModel @Inject constructor(
    private val appConfig: AppConfig,
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
    @ApplicationContext private val appContext: Context,
    private val sharedPreferencesStore: SharedPreferencesStore,
) : ViewModel(), DydxViewModel {

    private val mutableState = MutableStateFlow(createViewState())

    val state: Flow<SettingsView.ViewState?> = mutableState

    private fun createViewState(): SettingsView.ViewState {
        val settings = PlatformUISettings.loadFromAssets(appContext, "debug.json")
        return SettingsView.ViewState.createFrom(
            settings = settings,
            localizer = localizer,
            header = "Debug",
            footer = appConfig.appVersionName + " (" + appConfig.appVersionCode + ")",
            backAction = {
                router.navigateBack()
            },
            itemAction = { link ->
                router.navigateTo(link)
            },
            valueOfField = { field ->
                val fieldId = field.field
                return@createFrom if (fieldId != null) {
                    sharedPreferencesStore.read(fieldId)
                } else {
                    null
                }
            },
            itemFieldAction = { fieldId, value ->
                sharedPreferencesStore.save(value, fieldId)
                mutableState.value = createViewState()
            },
        )
    }
}
