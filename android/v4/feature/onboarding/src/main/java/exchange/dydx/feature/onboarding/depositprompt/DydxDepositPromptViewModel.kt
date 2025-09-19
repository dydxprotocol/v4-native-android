package exchange.dydx.feature.onboarding.depositprompt

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.TransferRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxDepositPromptViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxDepositPromptView.ViewState?> = abacusStateManager.state.currentWallet
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(
        wallet: DydxWalletInstance?
    ): DydxDepositPromptView.ViewState {
        val loginMode = DydxDepositPromptView.LoginMode.fromString(wallet?.loginMethod)
        return DydxDepositPromptView.ViewState(
            localizer = localizer,
            loginMode = loginMode,
            user = if (loginMode == DydxDepositPromptView.LoginMode.apple) {
                "Apple User"
            } else {
                wallet?.userEmail ?: ""
            },
            ctaAction = {
                router.navigateBack()
                router.navigateTo(
                    route = TransferRoutes.transfer_turnkey_deposit,
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
            closeAction = {
                router.navigateBack()
            },
        )
    }
}
