package exchange.dydx.trading.feature.vault.depositwithdraw

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.vault.VaultInputState
import exchange.dydx.trading.feature.vault.VaultInputType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class DydxVaultDepositWithdrawSelectionViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val inputState: VaultInputState,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultDepositWithdrawSelectionView.ViewState?> =
        inputState.type
            .mapNotNull { it }
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        selection: VaultInputType
    ): DydxVaultDepositWithdrawSelectionView.ViewState {
        return DydxVaultDepositWithdrawSelectionView.ViewState(
            localizer = localizer,
            selections = listOf(
                DydxVaultDepositWithdrawSelectionView.Selection.Deposit,
                DydxVaultDepositWithdrawSelectionView.Selection.Withdrawal,
            ),
            currentSelection = when (selection) {
                VaultInputType.DEPOSIT -> DydxVaultDepositWithdrawSelectionView.Selection.Deposit
                VaultInputType.WITHDRAW -> DydxVaultDepositWithdrawSelectionView.Selection.Withdrawal
            },
            onSelectionChanged = { selection ->
                inputState.type.value = when (selection) {
                    DydxVaultDepositWithdrawSelectionView.Selection.Deposit -> VaultInputType.DEPOSIT
                    DydxVaultDepositWithdrawSelectionView.Selection.Withdrawal -> VaultInputType.WITHDRAW
                }
                inputState.amount.value = null
                inputState.slippageAcked.value = false
            },
        )
    }
}
