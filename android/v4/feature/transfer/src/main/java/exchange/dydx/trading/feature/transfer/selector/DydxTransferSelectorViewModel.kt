package exchange.dydx.trading.feature.transfer.selector

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.TransferRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxTransferSelectorViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTransferSelectorView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxTransferSelectorView.ViewState {
        return DydxTransferSelectorView.ViewState(
            localizer = localizer,
            isMainnet = abacusStateManager.state.isMainNet,
            closeAction = {
                router.navigateBack()
            },
            onActionTapped = { action ->
                router.navigateBack()
                when (action) {
                    DydxTransferSelectorView.Action.Deposit -> {
                        router.navigateTo(
                            route = TransferRoutes.transfer_deposit,
                            presentation = DydxRouter.Presentation.Modal,
                        )
                    }
                    DydxTransferSelectorView.Action.Withdrawal -> {
                        router.navigateTo(
                            route = TransferRoutes.transfer_withdrawal,
                            presentation = DydxRouter.Presentation.Modal,
                        )
                    }
                    DydxTransferSelectorView.Action.TransferOut -> {
                        router.navigateTo(
                            route = TransferRoutes.transfer_out,
                            presentation = DydxRouter.Presentation.Modal,
                        )
                    }
                    DydxTransferSelectorView.Action.Faucet -> {
                        router.navigateTo(
                            route = TransferRoutes.transfer_faucet,
                            presentation = DydxRouter.Presentation.Modal,
                        )
                    }
                }
            },
        )
    }
}
