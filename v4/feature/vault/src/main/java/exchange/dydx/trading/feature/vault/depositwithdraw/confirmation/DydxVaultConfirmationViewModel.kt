package exchange.dydx.trading.feature.vault.depositwithdraw.confirmation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.functional.vault.VaultDepositData
import exchange.dydx.abacus.functional.vault.VaultFormValidationResult
import exchange.dydx.abacus.functional.vault.VaultWithdrawData
import exchange.dydx.abacus.output.Vault
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import exchange.dydx.trading.feature.vault.VaultInputStage
import exchange.dydx.trading.feature.vault.VaultInputState
import exchange.dydx.trading.feature.vault.VaultInputType
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@HiltViewModel
class DydxVaultConfirmationViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val inputState: VaultInputState,
    private val router: DydxRouter,
    private val parser: ParserProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultConfirmationView.ViewState?> =
        combine(
            abacusStateManager.state.vault,
            inputState.amount,
            inputState.type.filterNotNull(),
            inputState.result,
        ) { vault, amount, type, result ->
            createViewState(vault, amount, type, result)
        }

    private fun createViewState(
        vault: Vault?,
        amount: Double?,
        type: VaultInputType,
        result: VaultFormValidationResult?
    ): DydxVaultConfirmationView.ViewState {
        return DydxVaultConfirmationView.ViewState(
            localizer = localizer,
            direction = when (type) {
                VaultInputType.DEPOSIT -> DydxVaultConfirmationView.Direction.Deposit
                VaultInputType.WITHDRAW -> DydxVaultConfirmationView.Direction.Withdraw
            },
            headerTitle = when (type) {
                VaultInputType.DEPOSIT -> localizer.localize("APP.VAULTS.CONFIRM_DEPOSIT_CTA")
                VaultInputType.WITHDRAW -> localizer.localize("APP.VAULTS.CONFIRM_WITHDRAW_CTA")
            },
            sourceLabel = when (type) {
                VaultInputType.DEPOSIT -> localizer.localize("APP.VAULTS.AMOUNT_TO_DEPOSIT")
                VaultInputType.WITHDRAW -> localizer.localize("APP.VAULTS.AMOUNT_TO_WITHDRAW")
            },
            sourceValue = formatter.dollar(amount, digits = 2),
            destinationValue = when (type) {
                VaultInputType.DEPOSIT -> localizer.localize("APP.VAULTS.VAULT")
                VaultInputType.WITHDRAW -> localizer.localize("APP.VAULTS.CROSS_ACCOUNT")
            },
            destinationIcon = when (type) {
                VaultInputType.DEPOSIT -> R.drawable.vault_account_token
                VaultInputType.WITHDRAW -> R.drawable.vault_cross_token
            },
            backAction = {
                router.navigateBack()
                inputState.stage.value = VaultInputStage.EDIT
            },
            ctaButton = InputCtaButton.ViewState(
                localizer = localizer,
                ctaButtonState = InputCtaButton.State.Enabled(
                    when (type) {
                        VaultInputType.DEPOSIT -> localizer.localize("APP.VAULTS.CONFIRM_DEPOSIT_CTA")
                        VaultInputType.WITHDRAW -> localizer.localize("APP.VAULTS.CONFIRM_WITHDRAW_CTA")
                    },
                ),
                ctaAction = {
                    when (type) {
                        VaultInputType.DEPOSIT -> submitDeposit(result?.submissionData?.deposit)
                        VaultInputType.WITHDRAW -> submitWithdraw(result?.submissionData?.withdraw)
                    }
                },
            ),
        )
    }

    private fun submitDeposit(depositData: VaultDepositData?) {
        val depositData = depositData ?: return
        cosmosClient.depositToMegavault(
            subaccountNumber = parser.asInt(depositData.subaccountFrom) ?: 0,
            amountUsdc = depositData.amount,
            completion = { response ->
                print(response)
            },
        )
    }

    private fun submitWithdraw(withdrawData: VaultWithdrawData?) {
        val withdrawData = withdrawData ?: return
        cosmosClient.withdrawFromMegavault(
            subaccountNumber = parser.asInt(withdrawData.subaccountTo) ?: 0,
            shares = withdrawData.shares.toLong(),
            minAmount = withdrawData.minAmount.toLong(),
            completion = { response ->
                print(response)
            },
        )
    }
}
