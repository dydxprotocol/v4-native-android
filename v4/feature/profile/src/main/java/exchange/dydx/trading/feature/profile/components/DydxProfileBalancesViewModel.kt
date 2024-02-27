package exchange.dydx.trading.feature.profile.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusState
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.nativeTokenLogoUrl
import exchange.dydx.dydxstatemanager.nativeTokenName
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxProfileBalancesViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxProfileBalancesView.ViewState?> =
        combine(
            abacusStateManager.state.accountBalance(AbacusState.NativeTokenDenom.DYDX),
            abacusStateManager.state.stakingBalance(AbacusState.NativeTokenDenom.DYDX),
        ) { accountBalance, stakingBalance ->
            createViewState(accountBalance, stakingBalance)
        }
            .distinctUntilChanged()

    private fun createViewState(accountBalance: Double?, stakingBalance: Double?): DydxProfileBalancesView.ViewState {
        val walletAmount = if (accountBalance != null) {
            formatter.localFormatted(accountBalance, 4)
        } else {
            null
        }
        val stakedAmount = if (stakingBalance != null) {
            formatter.localFormatted(stakingBalance, 4)
        } else {
            null
        }
        return DydxProfileBalancesView.ViewState(
            localizer = localizer,
            nativeTokenName = abacusStateManager.nativeTokenName,
            nativeTokenLogoUrl = abacusStateManager.nativeTokenLogoUrl,
            walletAmount = walletAmount,
            stakedAmount = stakedAmount,
        )
    }
}
