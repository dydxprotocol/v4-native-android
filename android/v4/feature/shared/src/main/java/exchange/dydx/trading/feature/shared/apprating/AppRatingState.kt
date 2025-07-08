package exchange.dydx.trading.feature.shared.apprating

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.dydxstatemanager.clientState.apprating.DydxAppRatingState
import exchange.dydx.dydxstatemanager.clientState.apprating.DydxAppRatingStateManagerProtocol
import exchange.dydx.trading.common.featureflags.DydxBoolFeatureFlag
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.feature.shared.analytics.AppRatingAnalytics
import exchange.dydx.utilities.utils.Logging
import javax.inject.Inject

private val TAG = "AppRatingState"

@ActivityRetainedScoped
class AppRatingState @Inject constructor(
    private val appRatingStateManager: DydxAppRatingStateManagerProtocol,
    private val logger: Logging,
    private val analytics: AppRatingAnalytics,
    private val featureFlags: DydxFeatureFlags,
) {
    enum class ResponseType {
        POSITIVE,
        NEGATIVE,
        DISMISSED,
    }

    private val currentState: DydxAppRatingState?
        get() {
            return appRatingStateManager.state.value
        }

    val shouldShowDialog: Boolean
        get() {
            if (featureFlags.isFeatureEnabled(DydxBoolFeatureFlag.ff_prompt_app_rating) == false) {
                return false
            }

            val state = currentState ?: return false
            if (state.shouldStopPreprompting) {
                return false
            }

            if (state.hasEverConnectedWallet) {
                return state.uniqueDayAppOpensCount >= 8 ||
                    state.transfersCreatedSinceLastPrompt.size >= 2 ||
                    state.ordersCreatedSinceLastPrompt.size >= 8
            } else {
                return state.uniqueDayAppOpensCount >= 4
            }
        }

    fun connectedWallet() {
        if (featureFlags.isFeatureEnabled(DydxBoolFeatureFlag.ff_prompt_app_rating) == false) {
            return
        }
        val state = currentState ?: return
        appRatingStateManager.update(state.copy(hasEverConnectedWallet = true))
    }

    fun orderCreated(orderId: String, orderCreatedTimestampMillis: Double) {
        if (featureFlags.isFeatureEnabled(DydxBoolFeatureFlag.ff_prompt_app_rating) == false) {
            return
        }
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
        if (featureFlags.isFeatureEnabled(DydxBoolFeatureFlag.ff_prompt_app_rating) == false) {
            return
        }
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

    fun launchedApp() {
        if (featureFlags.isFeatureEnabled(DydxBoolFeatureFlag.ff_prompt_app_rating) == false) {
            return
        }
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

    fun prompt() {
        val state = currentState ?: return
        analytics.logPrompt(state)
    }

    fun prompted(response: ResponseType) {
        analytics.logPromptCompleted(response)
        when (response) {
            ResponseType.POSITIVE -> {
                reset()
                val state = currentState ?: return
                appRatingStateManager.update(
                    state.copy(
                        lastPromptedTimestamp = System.currentTimeMillis().toDouble(),
                        shouldStopPreprompting = true,
                    ),
                )
            }
            ResponseType.NEGATIVE -> {
                reset()
                val state = currentState ?: return
                appRatingStateManager.update(
                    state.copy(
                        lastPromptedTimestamp = System.currentTimeMillis().toDouble(),
                        shouldStopPreprompting = true,
                    ),
                )
            }
            ResponseType.DISMISSED -> {
                reset()
                val state = currentState ?: return
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

    fun startReviewFlow(context: Context) {
        val manager = ReviewManagerFactory.create(context as Activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                logger.d(TAG, "ReviewInfo: $reviewInfo")
                val flow = manager.launchReviewFlow(context, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    analytics.logCompleted(isSuccess = true, errorCode = null)
                }
            } else {
                // There was some problem, log or handle the error code.
                @ReviewErrorCode val reviewErrorCode =
                    (task.getException() as ReviewException).errorCode
                logger.d(TAG, "ReviewErrorCode: $reviewErrorCode")
                analytics.logCompleted(isSuccess = false, errorCode = reviewErrorCode)
            }
        }
    }
}
