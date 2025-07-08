package exchange.dydx.trading.feature.shared.analytics

import exchange.dydx.abacus.functional.ClientTrackableEventType
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.di.CoroutineDispatchers
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.plus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingAnalytics @Inject constructor(
    private val tracker: Tracking,
    private val abacusStateManager: AbacusStateManagerProtocol,
    @CoroutineScopes.App private val appScope: CoroutineScope,
    @CoroutineDispatchers.IO private val ioDispatcher: CoroutineDispatcher,
) {
    // The three main OnboardingStates:
    // - Disconnected
    // - WalletConnected
    // - AccountConnected
    private enum class OnboardingState(val rawValue: String) {
        // User is disconnected.
        DISCONNECTED("Disconnected"),

        // Wallet is connected.
        WALLET_CONNECTED("WalletConnected"),

        // Account is connected.
        ACCOUNT_CONNECTED("AccountConnected")
    }

    // Enum representing the various steps in the onboarding process.
    enum class OnboardingSteps(val rawValue: String) {
        // Step: Choose Wallet
        CHOOSE_WALLET("ChooseWallet"),

        // Step: Key Derivation
        KEY_DERIVATION("KeyDerivation"),

        // Step: Acknowledge Terms
        ACKNOWLEDGE_TERMS("AcknowledgeTerms"),

        DEPOSIT_INITIATED("TransferDepositFundsClick"),

        // Step: Deposit Funds
        DEPOSIT_FUNDS("DepositFunds")
    }

    private val scope = appScope + ioDispatcher

    fun log(step: OnboardingSteps) {
        abacusStateManager.state.currentWallet
            .take(1)
            .onEach {
                val state = when {
                    it == null -> OnboardingState.DISCONNECTED
                    it.cosmoAddress.isNullOrEmpty() -> OnboardingState.ACCOUNT_CONNECTED
                    else -> OnboardingState.WALLET_CONNECTED
                }

                tracker.logSharedEvent(
                    ClientTrackableEventType.OnboardingStepChanged(
                        state = state.rawValue,
                        step = step.rawValue,
                    ),
                )
            }
            .launchIn(scope)
    }
}
