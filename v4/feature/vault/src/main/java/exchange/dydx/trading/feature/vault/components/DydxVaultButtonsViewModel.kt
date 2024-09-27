package exchange.dydx.trading.feature.vault.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxVaultButtonsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultButtonsView.ViewState?> = abacusStateManager.state.marketSummary
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(marketSummary: PerpetualMarketSummary?): DydxVaultButtonsView.ViewState {
        val volume = formatter.dollarVolume(marketSummary?.volume24HUSDC)
        return DydxVaultButtonsView.ViewState(
            localizer = localizer,
            depositAction = {
                cosmosClient.depositToMegavault(
                    subaccountNumber = 0,
                    amountUsdc = 1.0,
                    completion = { response ->
                        print(response)
                    },
                )
                //  router.navigateTo(route = VaultRoutes.deposit, presentation = Presentation.Modal)
            },
            withdrawAction = {
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
                //  router.navigateTo(route = VaultRoutes.withdraw, presentation = Presentation.Modal)
            },
        )
    }
}
