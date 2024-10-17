package exchange.dydx.trading.feature.vault.receipt

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.functional.vault.VaultFormValidationResult
import exchange.dydx.abacus.output.Vault
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.receipt.components.DydxReceiptItemView
import exchange.dydx.trading.feature.receipt.components.buyingpower.DydxReceiptFreeCollateralView
import exchange.dydx.trading.feature.receipt.components.marginusage.DydxReceiptMarginUsageView
import exchange.dydx.trading.feature.shared.views.AmountText
import exchange.dydx.trading.feature.shared.views.MarginUsageView
import exchange.dydx.trading.feature.vault.VaultInputStage
import exchange.dydx.trading.feature.vault.VaultInputState
import exchange.dydx.trading.feature.vault.VaultInputType
import exchange.dydx.trading.feature.vault.receipt.DydxVaultReceiptView.VaultReceiptLineType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class DydxVaultReceiptViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val inputState: VaultInputState,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultReceiptView.ViewState?> =
        combine(
            abacusStateManager.state.selectedSubaccount,
            abacusStateManager.state.vault,
            inputState.type,
            inputState.stage,
            inputState.result,
        ) { subaccount, vault, type, stage, result ->
            when (type) {
                VaultInputType.DEPOSIT -> createDepositViewState(subaccount, vault, stage, result)
                VaultInputType.WITHDRAW -> createWithdrawViewState(subaccount, vault, stage, result)
                else -> null
            }
        }

    private fun createDepositViewState(
        subaccount: Subaccount?,
        vault: Vault?,
        stage: VaultInputStage,
        result: VaultFormValidationResult?
    ): DydxVaultReceiptView.ViewState {
        return DydxVaultReceiptView.ViewState(
            localizer = localizer,
            lineTypes = listOfNotNull(
                if (stage == VaultInputStage.CONFIRM) VaultReceiptLineType.FreeCollateral else null,
                VaultReceiptLineType.MarginUsage,
                VaultReceiptLineType.Balance,
            ),
            freeCollateral = createFreeCollateralViewState(subaccount, result),
            marginUsage = createMarginUsageViewState(subaccount, result),
            balance = createBalanceViewState(vault, result),
        )
    }

    private fun createWithdrawViewState(
        subaccount: Subaccount?,
        vault: Vault?,
        stage: VaultInputStage,
        result: VaultFormValidationResult?
    ): DydxVaultReceiptView.ViewState {
        return DydxVaultReceiptView.ViewState(
            localizer = localizer,
            lineTypes = listOfNotNull(
                VaultReceiptLineType.FreeCollateral,
                if (stage == VaultInputStage.CONFIRM) VaultReceiptLineType.Balance else null,
                VaultReceiptLineType.Slippage,
                VaultReceiptLineType.AmountReceived,
            ),
            freeCollateral = createFreeCollateralViewState(subaccount, result),
            balance = createBalanceViewState(vault, result),
            slippage = DydxReceiptItemView.ViewState(
                localizer = localizer,
                title = localizer.localize("APP.VAULTS.EST_SLIPPAGE"),
                value = formatter.percent(result?.summaryData?.estimatedSlippage, digits = 2),
            ),
            amountReceived = DydxReceiptItemView.ViewState(
                localizer = localizer,
                title = localizer.localize("APP.WITHDRAW_MODAL.EXPECTED_AMOUNT_RECEIVED"),
                value = formatter.dollar(result?.summaryData?.estimatedAmountReceived, digits = 2),
            ),
        )
    }

    private fun createFreeCollateralViewState(
        subaccount: Subaccount?,
        result: VaultFormValidationResult?
    ): DydxReceiptFreeCollateralView.ViewState {
        return DydxReceiptFreeCollateralView.ViewState(
            localizer = localizer,
            before = subaccount?.freeCollateral?.current?.let {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = it,
                    tickSize = 2,
                    requiresPositive = true,
                )
            },
            after = result?.summaryData?.freeCollateral?.let {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = it,
                    tickSize = 2,
                    requiresPositive = true,
                )
            },
        )
    }

    private fun createBalanceViewState(
        vault: Vault?,
        result: VaultFormValidationResult?
    ): DydxReceiptFreeCollateralView.ViewState {
        return DydxReceiptFreeCollateralView.ViewState(
            localizer = localizer,
            label = localizer.localize("APP.VAULTS.YOUR_VAULT_BALANCE"),
            before = vault?.account?.balanceUsdc?.let {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = it,
                    tickSize = 2,
                    requiresPositive = true,
                )
            },
            after = result?.summaryData?.vaultBalance?.let {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = it,
                    tickSize = 2,
                    requiresPositive = true,
                )
            },
        )
    }

    private fun createMarginUsageViewState(
        subaccount: Subaccount?,
        result: VaultFormValidationResult?
    ): DydxReceiptMarginUsageView.ViewState {
        return DydxReceiptMarginUsageView.ViewState(
            localizer = localizer,
            formatter = formatter,
            before = subaccount?.marginUsage?.current?.let {
                MarginUsageView.ViewState(
                    localizer = localizer,
                    percent = it,
                    displayOption = MarginUsageView.DisplayOption.IconAndValue,
                )
            },
            after = result?.summaryData?.marginUsage?.let {
                MarginUsageView.ViewState(
                    localizer = localizer,
                    percent = it,
                    displayOption = MarginUsageView.DisplayOption.IconAndValue,
                )
            },
        )
    }
}
