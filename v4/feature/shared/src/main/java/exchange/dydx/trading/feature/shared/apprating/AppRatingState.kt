package exchange.dydx.trading.feature.shared.apprating

import android.app.Application
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.dydxstatemanager.clientState.apprating.DydxAppRatingState
import exchange.dydx.dydxstatemanager.clientState.apprating.DydxAppRatingStateManagerProtocol
import exchange.dydx.trading.feature.shared.analytics.AppRatingAnalytics
import exchange.dydx.utilities.utils.Logging
import javax.inject.Inject

private val TAG = "AppRatingState"

@ActivityRetainedScoped
class AppRatingState @Inject constructor(
    private val appRatingStateManager: DydxAppRatingStateManagerProtocol,
    private val application: Application,
    private val logger: Logging,
    private val analytics: AppRatingAnalytics,
) {
    enum class ResponseType {
        POSITIVE,
        NEGATIVE,
        DISMISSED,
    }

    private val currentState: DydxAppRatingState?
        get() = appRatingStateManager.state.value

    val shouldShowDialog: Boolean
        get() {
            val state = currentState ?: return false
            return true
            if (state.shouldStopPreprompting) {
                return false
            }

            if (state.hasEverConnectedWallet) {
                return state.uniqueDayAppOpensCount >= 8 ||
                    state.transfersCreatedSinceLastPrompt.size >= 2 &&
                    state.ordersCreatedSinceLastPrompt.size >= 8 &&
                    state.vaultOperationsSinceLastPrompt.size >= 2
            } else {
                return state.uniqueDayAppOpensCount >= 4
            }
        }

    fun connectedWallet() {
        val state = currentState ?: return
        appRatingStateManager.update(state.copy(hasEverConnectedWallet = true))
    }

    fun orderCreated(orderId: String, orderCreatedTimestampMillis: Double) {
        var state = currentState ?: return
        if (orderCreatedTimestampMillis > state.lastPromptedTimestamp) {
            if (!state.ordersCreatedSinceLastPrompt.contains(orderId)) {
                state = state.copy(
                    ordersCreatedSinceLastPrompt = state.ordersCreatedSinceLastPrompt + orderId,
                )
                appRatingStateManager.update(state)
            }
        }
    }

    fun transferCreated(transferId: String, transferCreatedTimestampMillis: Double) {
        var state = currentState ?: return
        if (transferCreatedTimestampMillis > state.lastPromptedTimestamp) {
            if (!state.transfersCreatedSinceLastPrompt.contains(transferId)) {
                state = state.copy(
                    transfersCreatedSinceLastPrompt = state.transfersCreatedSinceLastPrompt + transferId,
                )
                appRatingStateManager.update(state)
            }
        }
    }

    fun vaultOperationCreated(vaultOperationId: String, vaultOperationCreatedTimestampMillis: Double) {
        var state = currentState ?: return
        if (vaultOperationCreatedTimestampMillis > state.lastPromptedTimestamp) {
            if (!state.vaultOperationsSinceLastPrompt.contains(vaultOperationId)) {
                state = state.copy(
                    vaultOperationsSinceLastPrompt = state.vaultOperationsSinceLastPrompt + vaultOperationId,
                )
                appRatingStateManager.update(state)
            }
        }
    }

    fun launchedApp() {
        val state = currentState ?: return
        val currentTime = System.currentTimeMillis().toDouble()
        if (currentTime - state.lastAppOpenTimestamp > 24 * 60 * 60 * 1000) {
            appRatingStateManager.update(
                state.copy(
                    uniqueDayAppOpensCount = state.uniqueDayAppOpensCount + 1,
                    lastAppOpenTimestamp = currentTime,
                ),
            )
        }
    }

    fun prompted(response: ResponseType) {
        val state = currentState ?: return
        analytics.logPrompted(response)
        when (response) {
            ResponseType.POSITIVE -> {
                appRatingStateManager.update(
                    state.copy(
                        lastPromptedTimestamp = System.currentTimeMillis().toDouble(),
                        shouldStopPreprompting = true,
                    ),
                )
                startReviewFlow()
            }
            ResponseType.NEGATIVE -> {
                reset()
                appRatingStateManager.update(
                    state.copy(
                        lastPromptedTimestamp = System.currentTimeMillis().toDouble(),
                    ),
                )
            }
            ResponseType.DISMISSED -> {
                reset()
                appRatingStateManager.update(
                    state.copy(
                        lastPromptedTimestamp = System.currentTimeMillis().toDouble(),
                    ),
                )
            }
        }
    }

    private fun reset() {
        val hasEverConnectedWallet = currentState?.hasEverConnectedWallet == true
        appRatingStateManager.reset()
        val state = currentState ?: return
        appRatingStateManager.update(
            state.copy(
                hasEverConnectedWallet = hasEverConnectedWallet,
            ),
        )
    }

    private fun startReviewFlow() {
        val manager = ReviewManagerFactory.create(application)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                logger.d(TAG, "ReviewInfo: $reviewInfo")
                analytics.logCompleted(isSuccess = true, errorCode = null)
            } else {
                // There was some problem, log or handle the error code.
                @ReviewErrorCode val reviewErrorCode = (task.getException() as ReviewException).errorCode
                logger.d(TAG, "ReviewErrorCode: $reviewErrorCode")
                analytics.logCompleted(isSuccess = false, errorCode = reviewErrorCode)
            }
        }
    }
}
