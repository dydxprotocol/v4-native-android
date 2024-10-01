package exchange.dydx.trading.feature.vault.depositwithdraw.deposit

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.functional.vault.VaultFormValidationResult
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
import exchange.dydx.trading.feature.vault.VaultInputState
import exchange.dydx.trading.feature.vault.depositwithdraw.components.VaultAmountBox
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class DydxVaultDepositViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val parser: ParserProtocol,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val inputState: VaultInputState,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultDepositView.ViewState?> =
        combine(
            abacusStateManager.state.selectedSubaccount,
            inputState.result,
        ) { subaccount, result ->
            createViewState(subaccount, result)
        }

    private fun createViewState(
        subaccount: Subaccount?,
        result: VaultFormValidationResult?
    ): DydxVaultDepositView.ViewState {
        return DydxVaultDepositView.ViewState(
            localizer = localizer,
            transferAmount = VaultAmountBox.ViewState(
                localizer = localizer,
                formatter = formatter,
                parser = parser,
                value = parser.asString(inputState.amount.value),
                maxAmount = subaccount?.freeCollateral?.current,
                maxAction = {
                    inputState.amount.value = subaccount?.freeCollateral?.current
                },
                title = localizer.localize("APP.VAULTS.ENTER_AMOUNT_TO_DEPOSIT"),
                footer = localizer.localize("APP.GENERAL.CROSS_FREE_COLLATERAL"),
                footerBefore = AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = subaccount?.freeCollateral?.current,
                    tickSize = 2,
                    requiresPositive = true,
                ),
                footerAfter = AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = result?.summaryData?.freeCollateral,
                    tickSize = 2,
                    requiresPositive = true,
                ),
                onEditAction = { amount ->
                    inputState.amount.value = parser.asDouble(amount)
                },
            ),
            ctaButton = InputCtaButton.ViewState(
                localizer = localizer,
                ctaButtonState = InputCtaButton.State.Enabled(localizer.localize("APP.VAULTS.PREVIEW_DEPOSIT")),
                ctaAction = {
//                    cosmosClient.depositToMegavault(
//                        subaccountNumber = 0,
//                        amountUsdc = 20.0,
//                        completion = { response ->
//                            print(response)
//                        },
//                    )

                    router.navigateTo(route = VaultRoutes.confirmation, presentation = DydxRouter.Presentation.Push)
                },
            ),
        )
    }
}
