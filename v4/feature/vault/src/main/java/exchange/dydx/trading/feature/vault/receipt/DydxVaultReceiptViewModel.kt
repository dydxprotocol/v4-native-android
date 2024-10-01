package exchange.dydx.trading.feature.vault.receipt

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.functional.vault.VaultFormValidationResult
import exchange.dydx.abacus.output.Vault
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.receipt.components.DydxReceiptItemView
import exchange.dydx.trading.feature.receipt.components.buyingpower.DydxReceiptFreeCollateralView
import exchange.dydx.trading.feature.receipt.components.marginusage.DydxReceiptMarginUsageView
import exchange.dydx.trading.feature.shared.views.AmountText
import exchange.dydx.trading.feature.shared.views.MarginUsageView
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
    private val parser: ParserProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultReceiptView.ViewState?> =
        combine(
            abacusStateManager.state.selectedSubaccount,
            abacusStateManager.state.vault,
            inputState.type,
            inputState.result,
        ) { subaccount, vault, type, result ->
            when (type) {
                VaultInputType.DEPOSIT -> createDepositViewState(subaccount, vault, result)
                VaultInputType.WITHDRAW -> createWithdrawViewState(subaccount, result)
                else -> null
            }
        }

    private fun createDepositViewState(
        subaccount: Subaccount?,
        vault: Vault?,
        result: VaultFormValidationResult?
    ): DydxVaultReceiptView.ViewState {
        return DydxVaultReceiptView.ViewState(
            localizer = localizer,
            lineTypes = listOf(
                VaultReceiptLineType.MarginUsage,
                VaultReceiptLineType.Balance,
            ),
            marginUsage = DydxReceiptMarginUsageView.ViewState(
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
            ),
            balance = DydxReceiptFreeCollateralView.ViewState(
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
            ),
        )
    }

    private fun createWithdrawViewState(
        subaccount: Subaccount?,
        result: VaultFormValidationResult?
    ): DydxVaultReceiptView.ViewState {
        return DydxVaultReceiptView.ViewState(
            localizer = localizer,
            lineTypes = listOf(
                VaultReceiptLineType.FreeCollateral,
                VaultReceiptLineType.Slippage,
                VaultReceiptLineType.AmountReceived,
            ),
            freeCollateral = DydxReceiptFreeCollateralView.ViewState(
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
            ),
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
}
