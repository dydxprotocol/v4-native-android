package exchange.dydx.trading.feature.profile.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.platformui.components.PlatformDialog
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.common.navigation.ProfileRoutes
import exchange.dydx.trading.feature.shared.analytics.WalletAnalytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxProfileButtonsViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val logoutDialog: PlatformDialog,
    private val walletAnalytics: WalletAnalytics,
    private val featureFlags: DydxFeatureFlags,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxProfileButtonsView.ViewState?> =
        combine(
            abacusStateManager.state.onboarded,
            abacusStateManager.state.currentWallet,
        ) { onboarded, wallet ->
            createViewState(onboarded, wallet)
        }
            .distinctUntilChanged()

    private fun createViewState(
        onboarded: Boolean,
        currentWallet: DydxWalletInstance?
    ): DydxProfileButtonsView.ViewState {
        val onboardAction: (() -> Unit)? = if (onboarded) {
            null
        } else {
            {
                router.navigateTo(
                    route = OnboardingRoutes.welcome,
                    presentation = DydxRouter.Presentation.Modal,
                )
            }
        }

        val signOutAction: (() -> Unit)? = if (onboarded) {
            {
                logoutDialog.showMessage(
                    title = localizer.localize("APP.GENERAL.SIGN_OUT"),
                    message = localizer.localize("APP.GENERAL.SIGN_OUT_WARNING"),
                    cancelTitle = localizer.localize("APP.GENERAL.CANCEL"),
                    confirmTitle = localizer.localize("APP.GENERAL.SIGN_OUT"),
                    confirmAction = {
                        walletAnalytics.logDisconnected(currentWallet?.walletId)
                        abacusStateManager.logOut()
                    },
                )
            }
        } else {
            null
        }

        val folder = abacusStateManager.environment?.walletConnection?.images
        return DydxProfileButtonsView.ViewState(
            localizer = localizer,
            walletImageUrl = if (folder != null) currentWallet?.imageUrl(folder) else null,
            onboarded = onboarded,
            settingsAction = {
                router.navigateTo(
                    route = ProfileRoutes.settings,
                    presentation = DydxRouter.Presentation.Push,
                )
            },
            helpAction = {
                router.navigateTo(
                    route = ProfileRoutes.help,
                    presentation = DydxRouter.Presentation.Push,
                )
            },
            walletAction = {
                if (!onboarded || currentWallet == null) {
                    router.navigateTo(
                        route = OnboardingRoutes.landing(featureFlags = featureFlags),
                        presentation = DydxRouter.Presentation.Modal,
                    )
                } else {
                    router.navigateTo(
                        route = if (currentWallet.walletId == "turnkey") ProfileRoutes.security else ProfileRoutes.wallets,
                        presentation = DydxRouter.Presentation.Modal,
                    )
                }
            },
            onboardAction = onboardAction,
            signOutAction = signOutAction,
            platformDialog = logoutDialog,
        )
    }
}
