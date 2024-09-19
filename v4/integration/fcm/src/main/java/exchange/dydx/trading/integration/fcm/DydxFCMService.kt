package exchange.dydx.trading.integration.fcm

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import exchange.dydx.dydxstatemanager.AbacusStateManager
import exchange.dydx.dydxstatemanager.protocolImplementations.AbacusLocalizerImp
import exchange.dydx.platformui.components.container.PlatformInfo
import javax.inject.Inject

@AndroidEntryPoint
class DydxFCMService : FirebaseMessagingService() {

    @Inject internal lateinit var fcmRegistrar: FCMRegistrar

    @Inject internal lateinit var platformInfo: PlatformInfo

    override fun onCreate() {
        super.onCreate()
        fcmRegistrar.registerToken()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        fcmRegistrar.registerToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.notification?.let { notification ->
            platformInfo.show(
                title = notification.title,
                message = notification.body,
            )
        }
    }
}

class FCMRegistrar @Inject constructor(
    private val application: Application,
    private val abacusStateManager: AbacusStateManager,
    private val abacusLocalizerImp: AbacusLocalizerImp,
) {

    fun registerToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(
            OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                registerToken(task.result)
            },
        )
    }

    fun registerToken(token: String) {
        // Only register token if permission has been granted.
        if (ContextCompat.checkSelfPermission(application, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("FCMRegistrar", "registering token: $token")
            abacusStateManager.registerPushToken(token, abacusLocalizerImp.language ?: "en")
        }
    }
}
