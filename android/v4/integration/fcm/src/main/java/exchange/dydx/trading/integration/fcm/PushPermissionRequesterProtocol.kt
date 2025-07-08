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
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.utilities.utils.ActivityDelegate
import exchange.dydx.utilities.utils.SharedPreferencesStore
import javax.inject.Inject

@ActivityRetainedScoped
class PushPermissionRequester @Inject constructor(
    private val platformInfo: PlatformInfo,
    private val abacusLocalizerImp: AbacusLocalizerImp,
    private val sharedPreferencesStore: SharedPreferencesStore,
) : PushPermissionRequesterProtocol {

    init {
        sharedPreferencesStore.save("false", PRIMER_SHOWN_KEY)
    }

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

    override val shouldRequestPermission: Boolean
        get() {
            val localActivity = activity ?: return false
            //  // This is only necessary for API level >= 33 (TIRAMISU)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionStatus = ContextCompat.checkSelfPermission(
                    localActivity,
                    Manifest.permission.POST_NOTIFICATIONS,
                )
                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }

            return sharedPreferencesStore.read(PRIMER_SHOWN_KEY) != "true"
        }

    // Mostly copy pasted from Firebase docs
    override fun requestPushPermission() {
        val localActivity = activity ?: return
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(localActivity, Manifest.permission.POST_NOTIFICATIONS)
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                return
            } else if (sharedPreferencesStore.read(PRIMER_SHOWN_KEY) != "true") {
                doRequest()
                sharedPreferencesStore.save("true", PRIMER_SHOWN_KEY)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun doRequest() {
        requestPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

interface PushPermissionRequesterProtocol : ActivityDelegate {
    fun requestPushPermission()
    val shouldRequestPermission: Boolean
}

@InstallIn(ActivityRetainedComponent::class)
@Module
interface PushPermissionRequesterModule {
    @Binds fun bindPushPermissionRequester(real: PushPermissionRequester): PushPermissionRequesterProtocol
}

const val PRIMER_SHOWN_KEY = "push_primer_shown"
