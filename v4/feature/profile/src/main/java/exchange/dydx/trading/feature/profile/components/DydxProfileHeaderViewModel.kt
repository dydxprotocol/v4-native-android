package exchange.dydx.trading.feature.profile.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletState
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxProfileHeaderViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxProfileHeaderView.ViewState?> = abacusStateManager.state.walletState
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(walletState: DydxWalletState?): DydxProfileHeaderView.ViewState {
        return DydxProfileHeaderView.ViewState(
            localizer = localizer,
            dydxChainLogoUrl = abacusStateManager.environment?.chainLogo,
            dydxAddress = walletState?.currentWallet?.cosmoAddress,
            onTapAction = {
            },
        )
    }
}
