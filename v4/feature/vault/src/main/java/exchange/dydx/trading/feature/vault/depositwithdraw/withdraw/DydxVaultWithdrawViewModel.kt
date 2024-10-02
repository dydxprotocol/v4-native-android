package exchange.dydx.trading.feature.vault.depositwithdraw.withdraw

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.functional.vault.VaultAccount
import exchange.dydx.abacus.functional.vault.VaultDepositWithdrawFormValidator
import exchange.dydx.abacus.functional.vault.VaultFormValidationResult
import exchange.dydx.abacus.output.Vault
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.VaultRoutes
import exchange.dydx.trading.feature.shared.views.AmountText
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import exchange.dydx.trading.feature.vault.VaultInputStage
import exchange.dydx.trading.feature.vault.VaultInputState
import exchange.dydx.trading.feature.vault.depositwithdraw.components.VaultAmountBox
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.schedule
import kotlin.math.abs

@HiltViewModel
class DydxVaultWithdrawViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val parser: ParserProtocol,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val inputState: VaultInputState,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    private var slippageRequestTimer: Timer? = null

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
                    updateAmount(value = vault?.account?.withdrawableUsdc, vaultAccount = vault?.account)
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
                    updateAmount(value = parser.asDouble(amount), vaultAccount = vault?.account)
                },
            ),
            ctaButton = InputCtaButton.ViewState(
                localizer = localizer,
                ctaButtonState = InputCtaButton.State.Enabled(localizer.localize("APP.VAULTS.PREVIEW_WITHDRAW")),
                ctaAction = {
                    inputState.stage.value = VaultInputStage.CONFIRM
                    router.navigateTo(route = VaultRoutes.confirmation, presentation = DydxRouter.Presentation.Push)
                },
            ),
        )
    }

    private fun updateAmount(
        value: Double?,
        vaultAccount: VaultAccount?
    ) {
        val currentValue = inputState.amount.value
        val significantChange = if (currentValue != null && value != null) {
            abs(currentValue - value) > 0.1
        } else {
            true
        }
        val shareValue = vaultAccount?.shareValue
        if (significantChange && value != null && shareValue != null && shareValue > 0) {
            val shares = value / shareValue
            requestSlippage(shares.toLong())
        }
        inputState.amount.value = value
    }

    private fun requestSlippage(shares: Long) {
        slippageRequestTimer = Timer()
        slippageRequestTimer?.schedule(delay = 1000L) {
            cosmosClient.getMegavaultWithdrawalInfo(
                shares = shares,
                completion = { response ->
                    if (response != null) {
                        inputState.slippageResponse.value = VaultDepositWithdrawFormValidator.getVaultDepositWithdrawSlippageResponse(response)
                    } else {
                        inputState.slippageResponse.value = null
                    }
                },
            )
        }
    }
}
