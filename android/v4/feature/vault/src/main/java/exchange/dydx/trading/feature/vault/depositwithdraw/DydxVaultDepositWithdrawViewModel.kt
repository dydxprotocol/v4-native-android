package exchange.dydx.trading.feature.vault.depositwithdraw

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.vault.VaultInputState
import exchange.dydx.trading.feature.vault.VaultInputType
import exchange.dydx.trading.feature.vault.depositwithdraw.DydxVaultDepositWithdrawView.DepositWithdrawType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class DydxVaultDepositWithdrawViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val inputState: VaultInputState,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    var type: DepositWithdrawType? = null
        set(value) {
            if (value != null && inputState.type.value == null) {
                inputState.reset()
                inputState.type.value = when (value) {
                    DepositWithdrawType.DEPOSIT -> VaultInputType.DEPOSIT
                    DepositWithdrawType.WITHDRAW -> VaultInputType.WITHDRAW
                }
            }
            field = value
        }

    val state: Flow<DydxVaultDepositWithdrawView.ViewState?> = inputState.type
        .mapNotNull { it }
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(
        selection: VaultInputType,
    ): DydxVaultDepositWithdrawView.ViewState {
        return DydxVaultDepositWithdrawView.ViewState(
            localizer = localizer,
            selection = when (selection) {
                VaultInputType.DEPOSIT -> DydxVaultDepositWithdrawSelectionView.Selection.Deposit
                VaultInputType.WITHDRAW -> DydxVaultDepositWithdrawSelectionView.Selection.Withdrawal
            },
            closeAction = {
                router.navigateBack()
            },
        )
    }
}
