package exchange.dydx.trading.feature.shared.apprating

import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.dydxstatemanager.clientState.apprating.DydxAppRatingState
import exchange.dydx.dydxstatemanager.clientState.apprating.DydxAppRatingStateManagerProtocol
import javax.inject.Inject

@ActivityRetainedScoped
class AppRatingState @Inject constructor(
    private val appRatingStateManager: DydxAppRatingStateManagerProtocol
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
                )
            )
        }
    }

    fun prompted(response: ResponseType) {
        val state = currentState ?: return
        when (response) {
            ResponseType.POSITIVE -> {
                appRatingStateManager.update(
                    state.copy(
                        lastPromptedTimestamp = System.currentTimeMillis().toDouble(),
                        shouldStopPreprompting = true,
                    )
                )
            }
            ResponseType.NEGATIVE -> {
                reset()
                appRatingStateManager.update(
                    state.copy(
                        lastPromptedTimestamp = System.currentTimeMillis().toDouble(),
                    )
                )
            }
            ResponseType.DISMISSED -> {
                reset()
                appRatingStateManager.update(
                    state.copy(
                        lastPromptedTimestamp = System.currentTimeMillis().toDouble(),
                    )
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
            )
        )
    }
}
