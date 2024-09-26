package exchange.dydx.trading.integration.fcm

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.dydxstatemanager.protocolImplementations.AbacusLocalizerImp
import exchange.dydx.platformui.components.PlatformDialog
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.utilities.utils.ActivityDelegate
import exchange.dydx.utilities.utils.SharedPreferencesStore
import javax.inject.Inject

@ActivityRetainedScoped
class PushPermissionRequester @Inject constructor(
    private val platformDialog: PlatformDialog,
    private val platformInfo: PlatformInfo,
    private val abacusLocalizerImp: AbacusLocalizerImp,
    private val sharedPreferencesStore: SharedPreferencesStore,
) : PushPermissionRequesterProtocol {

    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null

    override var activity: Activity? = null
        set(value) {
            requestPermissionLauncher?.unregister()
            field = value
            if (field == null) return

            requestPermissionLauncher = (field as ActivityResultCaller).registerForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { isGranted: Boolean ->
                if (isGranted) {
                    platformInfo.show(title = abacusLocalizerImp.localize("APP.PUSH_NOTIFICATIONS.ENABLED"))
                } else {
                    platformInfo.show(
                        title = abacusLocalizerImp.localize("APP.PUSH_NOTIFICATIONS.DISABLED"),
                        message = abacusLocalizerImp.localize("APP.PUSH_NOTIFICATIONS.DISABLED_BODY"),
                    )
                }
            }
        }

    // Mostly copy pasted from Firebase docs
    override fun requestPushPermission() {
        val localActivity = activity ?: return
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(localActivity, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Permission granted already. Do nothing
                return
            } else if (sharedPreferencesStore.read(PRIMER_SHOWN_KEY) != "true") {
                // Show primer if needed.
                platformDialog.showMessage(
                    title = abacusLocalizerImp.localize("APP.PUSH_NOTIFICATIONS_PRIMER_TITLE"),
                    message = abacusLocalizerImp.localize("APP.PUSH_NOTIFICATIONS_PRIMER_MESSAGE"),
                    confirmTitle = abacusLocalizerImp.localize("APP.GENERAL.OK"),
                    cancelTitle = abacusLocalizerImp.localize("APP.GENERAL.NOT_NOW"),
                    confirmAction = ::request,
                )
                sharedPreferencesStore.save("true", PRIMER_SHOWN_KEY)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun request() {
        requestPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

interface PushPermissionRequesterProtocol : ActivityDelegate {
    fun requestPushPermission()
}

@InstallIn(ActivityRetainedComponent::class)
@Module
interface PushPermissionRequesterModule {
    @Binds fun bindPushPermissionRequester(real: PushPermissionRequester): PushPermissionRequesterProtocol
}

private const val PRIMER_SHOWN_KEY = "push_primer_shown"
