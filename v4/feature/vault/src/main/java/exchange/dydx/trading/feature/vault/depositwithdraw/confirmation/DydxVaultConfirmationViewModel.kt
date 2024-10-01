package exchange.dydx.trading.feature.vault.depositwithdraw.confirmation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.functional.vault.VaultFormValidationResult
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.output.Vault
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import exchange.dydx.trading.feature.vault.VaultInputState
import exchange.dydx.trading.feature.vault.VaultInputType
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxVaultConfirmationViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val inputState: VaultInputState,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultConfirmationView.ViewState?> =
        combine(
            abacusStateManager.state.vault,
            inputState.type.filterNotNull(),
            inputState.result,
        ) {  vault, type, result ->
            createViewState(vault, type, result)
        }

    private fun createViewState(
        vault: Vault?,
        type: VaultInputType,
        result: VaultFormValidationResult?
    ): DydxVaultConfirmationView.ViewState {
        return DydxVaultConfirmationView.ViewState(
            localizer = localizer,
            headerTitle =  when (type) {
                VaultInputType.DEPOSIT -> localizer.localize("APP.GENERAL.CONFIRM_DEPOSIT")
                VaultInputType.WITHDRAW -> localizer.localize("APP.GENERAL.CONFIRM_WITHDRAW")
            },
            backAction = {
                router.navigateBack()
            },
            ctaButton = InputCtaButton.ViewState(
                localizer = localizer,
                ctaButtonState =  InputCtaButton.State.Enabled(
                    when (type) {
                        VaultInputType.DEPOSIT -> localizer.localize("APP.GENERAL.CONFIRM_DEPOSIT")
                        VaultInputType.WITHDRAW -> localizer.localize("APP.GENERAL.CONFIRM_WITHDRAW")
                    }
                ),
                ctaAction = {}
            )
        )
    }
}
