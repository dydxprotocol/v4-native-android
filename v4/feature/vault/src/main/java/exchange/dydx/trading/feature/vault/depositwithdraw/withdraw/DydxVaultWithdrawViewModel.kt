package exchange.dydx.trading.feature.vault.depositwithdraw.withdraw

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.functional.vault.VaultAccount
import exchange.dydx.abacus.functional.vault.VaultDepositWithdrawFormValidator
import exchange.dydx.abacus.functional.vault.VaultFormValidationResult
import exchange.dydx.abacus.output.Vault
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.VaultRoutes
import exchange.dydx.trading.feature.shared.analytics.VaultAnalytics
import exchange.dydx.trading.feature.shared.analytics.VaultAnalyticsInputType
import exchange.dydx.trading.feature.shared.views.AmountText
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import exchange.dydx.trading.feature.vault.VaultInputStage
import exchange.dydx.trading.feature.vault.VaultInputState
import exchange.dydx.trading.feature.vault.depositwithdraw.components.VaultAmountBox
import exchange.dydx.trading.feature.vault.depositwithdraw.createViewModel
import exchange.dydx.trading.feature.vault.displayedError
import exchange.dydx.trading.feature.vault.hasBlockingError
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class DydxVaultWithdrawViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val parser: ParserProtocol,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val inputState: VaultInputState,
    private val router: DydxRouter,
    @CoroutineScopes.ViewModel private val coroutineScope: CoroutineScope,
    private val vaultAnalytics: VaultAnalytics,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultWithdrawView.ViewState?> =
        combine(
            abacusStateManager.state.vault,
            inputState.result,
        ) { vault, result ->
            createViewState(vault, result)
        }

    private var slippageDebounce: Job? = null

    private fun createViewState(
        vault: Vault?,
        result: VaultFormValidationResult?
    ): DydxVaultWithdrawView.ViewState {
        val roundedWithdrawableUsdc = formatter.raw(vault?.account?.withdrawableUsdc, digits = 2, rounding = RoundingMode.DOWN)
        return DydxVaultWithdrawView.ViewState(
            localizer = localizer,
            transferAmount = VaultAmountBox.ViewState(
                localizer = localizer,
                formatter = formatter,
                parser = parser,
                value = parser.asString(inputState.amount.value),
                maxAmount = roundedWithdrawableUsdc?.toDouble(),
                maxAction = {
                    val amount = roundedWithdrawableUsdc?.toDouble()
                    updateAmount(value = amount, vaultAccount = vault?.account)
                },
                title = localizer.localize("APP.VAULTS.ENTER_AMOUNT_TO_WITHDRAW"),
                footer = localizer.localize("APP.VAULTS.YOUR_VAULT_BALANCE"),
                footerBefore = AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = roundedWithdrawableUsdc?.toDouble(),
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
            validation = result?.displayedError?.createViewModel(localizer),
            ctaButton = InputCtaButton.ViewState(
                localizer = localizer,
                ctaButtonState = if (result?.hasBlockingError == true || inputState.amount.value == null) {
                    InputCtaButton.State.Disabled(localizer.localize("APP.VAULTS.PREVIEW_WITHDRAW"))
                } else {
                    InputCtaButton.State.Enabled(localizer.localize("APP.VAULTS.PREVIEW_WITHDRAW"))
                },
                ctaAction = {
                    inputState.stage.value = VaultInputStage.CONFIRM
                    router.navigateTo(route = VaultRoutes.confirmation, presentation = DydxRouter.Presentation.Push)

                    vaultAnalytics.logPreview(
                        type = VaultAnalyticsInputType.WITHDRAW,
                        amount = inputState.amount.value ?: 0.0,
                    )
                },
            ),
        )
    }

    private fun updateAmount(
        value: Double?,
        vaultAccount: VaultAccount?
    ) {
        val shareValue = vaultAccount?.shareValue
        if (value != null && shareValue != null && shareValue > 0) {
            val shares = VaultDepositWithdrawFormValidator.calculateSharesToWithdraw(vaultAccount = vaultAccount, amount = value)
            requestSlippage(shares.toLong())
        }
        inputState.amount.value = value
    }

    private fun requestSlippage(shares: Long) {
        slippageDebounce?.cancel()
        slippageDebounce = coroutineScope.launch {
            delay(500L)
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
