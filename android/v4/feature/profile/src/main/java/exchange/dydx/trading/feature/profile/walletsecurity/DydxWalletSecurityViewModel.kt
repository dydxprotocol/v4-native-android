package exchange.dydx.trading.feature.profile.walletsecurity

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.KeyExportType
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.common.navigation.ProfileRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxWalletSecurityViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxWalletSecurityView.ViewState?> = abacusStateManager.state.currentWallet
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(
        wallet: DydxWalletInstance?
    ): DydxWalletSecurityView.ViewState {
        return DydxWalletSecurityView.ViewState(
            localizer = localizer,
            backButtonAction = {
                router.navigateBack()
            },
            loginAction = {
                router.navigateTo(route = OnboardingRoutes.turnkey, presentation = DydxRouter.Presentation.Push)
            },
            exportSourceAction = {
                router.navigateTo(
                    route = ProfileRoutes.key_export + "/${KeyExportType.source}",
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
            exportDydxAction = {
                router.navigateTo(
                    route = ProfileRoutes.key_export + "/${KeyExportType.dydx}",
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
            email = wallet?.userEmail,
            sourceAddress = wallet?.ethereumAddress,
            dydxAddress = wallet?.cosmoAddress,
            loginMethod = DydxWalletSecurityView.LoginMethod.fromString(wallet?.loginMethod),
        )
    }
}
