package exchange.dydx.trading.feature.vault.depositwithdraw.withdraw

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
import exchange.dydx.trading.feature.shared.views.AmountText
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import exchange.dydx.trading.feature.vault.VaultInputState
import exchange.dydx.trading.feature.vault.depositwithdraw.components.VaultAmountBox
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class DydxVaultWithdrawViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val parser: ParserProtocol,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val inputState: VaultInputState,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultWithdrawView.ViewState?> =
        combine(
            abacusStateManager.state.selectedSubaccount,
            abacusStateManager.state.vault,
            inputState.result,
        ) { subaccount, vault, result ->
            createViewState(subaccount, vault, result)
        }

    private fun createViewState(
        subaccount: Subaccount?,
        vault: Vault?,
        result: VaultFormValidationResult?
    ): DydxVaultWithdrawView.ViewState {
        return DydxVaultWithdrawView.ViewState(
            localizer = localizer,
            transferAmount = VaultAmountBox.ViewState(
                localizer = localizer,
                formatter = formatter,
                parser = parser,
                value = parser.asString(inputState.amount.value),
                maxAmount = vault?.account?.withdrawableUsdc,
                maxAction = {
                    inputState.amount.value = vault?.account?.withdrawableUsdc
                },
                title = localizer.localize("APP.VAULTS.ENTER_AMOUNT_TO_WITHDRAW"),
                footer = localizer.localize("APP.VAULTS.YOUR_VAULT_BALANCE"),
                footerBefore = AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = vault?.account?.withdrawableUsdc,
                    tickSize = 2,
                    requiresPositive = true,
                ),
                footerAfter = AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = result?.summaryData?.withdrawableVaultBalance,
                    tickSize = 2,
                    requiresPositive = true,
                ),
                onEditAction = { amount ->
                    inputState.amount.value = parser.asDouble(amount)
                },
            ),
            ctaButton = InputCtaButton.ViewState(
                localizer = localizer,
                ctaButtonState = InputCtaButton.State.Enabled(localizer.localize("APP.VAULTS.PREVIEW_WITHDRAW")),
                ctaAction = {
                    cosmosClient.getMegavaultWithdrawalInfo(
                        shares = 2,
                        completion = { response ->
                            print(response)
                        },
                    )
//                cosmosClient.withdrawFromMegavault(
//                    subaccountNumber = 0,
//                    shares = 2,
//                    minAmount = 0,
//                    completion = { response ->
//                        print(response)
//                    }
//                )
                },
            ),
        )
    }
}
