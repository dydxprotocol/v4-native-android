package exchange.dydx.trading.feature.profile.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.dydxTokenInfo
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
            abacusStateManager.state.accountBalance(abacusStateManager.environment?.dydxTokenInfo?.denom),
            abacusStateManager.state.stakingBalance(abacusStateManager.environment?.dydxTokenInfo?.denom),
        ) { accountBalance, stakingBalance ->
            createViewState(accountBalance, stakingBalance)
        }
            .distinctUntilChanged()

    private fun createViewState(accountBalance: Double?, stakingBalance: Double?): DydxProfileBalancesView.ViewState {
        val decimal = 4
        val walletAmount = if (accountBalance != null) {
            formatter.localFormatted(accountBalance, decimal)
        } else {
            null
        }
        val stakedAmount = if (stakingBalance != null) {
            formatter.localFormatted(stakingBalance, decimal)
        } else {
            null
        }
        return DydxProfileBalancesView.ViewState(
            localizer = localizer,
            nativeTokenName = abacusStateManager.nativeTokenName,
            nativeTokenLogoUrl = abacusStateManager.nativeTokenLogoUrl,
            walletAmount = walletAmount,
            stakedAmount = stakedAmount,
            totalAmount = if (walletAmount != null || stakedAmount != null) {
                formatter.localFormatted(
                    (walletAmount?.toDoubleOrNull() ?: 0.0) + (stakedAmount?.toDoubleOrNull() ?: 0.0),
                    decimal
                )
            } else {
                null
            },
        )
    }
}
