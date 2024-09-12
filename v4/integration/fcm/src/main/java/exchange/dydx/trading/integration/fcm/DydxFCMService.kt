package exchange.dydx.trading.integration.fcm

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import dagger.hilt.android.AndroidEntryPoint
import exchange.dydx.dydxstatemanager.AbacusStateManager
import javax.inject.Inject

@AndroidEntryPoint
class DydxFCMService : FirebaseMessagingService() {

    @Inject
    internal lateinit var fcmRegistrar: FCMRegistrar

    override fun onCreate() {
        super.onCreate()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(
            OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                fcmRegistrar.registerToken(token)
            },
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        fcmRegistrar.registerToken(token)
    }
}

internal class FCMRegistrar @Inject constructor(
    private val abacusStateManager: AbacusStateManager
) {
    fun registerToken(token: String) {
        Log.d("FCMRegistrar", "registering token: $token")
        // call abacus method to register token
    }
}
