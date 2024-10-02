package exchange.dydx.trading.feature.vault

import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.functional.vault.VaultDepositWithdrawFormValidator
import exchange.dydx.abacus.functional.vault.VaultFormAccountData
import exchange.dydx.abacus.functional.vault.VaultFormAction
import exchange.dydx.abacus.functional.vault.VaultFormData
import exchange.dydx.abacus.functional.vault.VaultFormValidationResult
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import indexer.models.chain.OnChainVaultDepositWithdrawSlippageResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

enum class VaultInputType {
    DEPOSIT,
    WITHDRAW
}

enum class VaultInputStage {
    EDIT,
    CONFIRM,
    SUBMIT
}

@ActivityRetainedScoped
class VaultInputState @Inject constructor(
    private val abacusStateManager: AbacusStateManagerProtocol,
) {
    val type: MutableStateFlow<VaultInputType?> = MutableStateFlow(null)
    val amount: MutableStateFlow<Double?> = MutableStateFlow(null)
    val stage: MutableStateFlow<VaultInputStage> = MutableStateFlow(VaultInputStage.EDIT)
    val slippageAcked: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val slippageResponse: MutableStateFlow<OnChainVaultDepositWithdrawSlippageResponse?> = MutableStateFlow(null)

    private val vaultFormData: Flow<VaultFormData?> =
        combine(
            type,
            amount,
            stage,
            slippageAcked,
        ) { type, amount, stage, slippageAcked ->
            val type = type ?: return@combine null
            val amount = amount ?: return@combine null
            VaultFormData(
                action = when (type) {
                    VaultInputType.DEPOSIT -> VaultFormAction.DEPOSIT
                    VaultInputType.WITHDRAW -> VaultFormAction.WITHDRAW
                },
                amount = amount,
                acknowledgedSlippage = slippageAcked,
                inConfirmationStep = stage == VaultInputStage.CONFIRM,
            )
        }
            .distinctUntilChanged()

    private val vaultFormAccountData: Flow<VaultFormAccountData?> =
        abacusStateManager.state.selectedSubaccount
            .map {
                VaultFormAccountData(
                    marginUsage = it?.marginUsage?.current,
                    freeCollateral = it?.freeCollateral?.current,
                    canViewAccount = it != null,
                )
            }
            .distinctUntilChanged()

    val result: Flow<VaultFormValidationResult?> =
        combine(
            vaultFormData,
            vaultFormAccountData,
            abacusStateManager.state.vault.map { it?.account },
            slippageResponse,
        ) { formData, accountData, account, slippageResponse ->
            val formData = formData ?: return@combine null
            VaultDepositWithdrawFormValidator.validateVaultForm(
                formData = formData,
                accountData = accountData,
                vaultAccount = account,
                slippageResponse = slippageResponse,
            )
        }
            .distinctUntilChanged()

    fun reset() {
        type.value = null
        amount.value = null
        stage.value = VaultInputStage.EDIT
        slippageAcked.value = false
        slippageResponse.value = null
    }
}
