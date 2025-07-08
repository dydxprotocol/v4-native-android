package exchange.dydx.trading.feature.transfer

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTransferSectionsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val selectionFlow: MutableStateFlow<DydxTransferSectionsView.Selection>,
    private val tracker: Tracking,
) : ViewModel(), DydxViewModel {

    init {
        // Initial section is Deposit
        tracker.log(
            event = "NavigateDialog",
            data = mapOf(
                "type" to "Deposit2",
            ),
        )
    }

    val state: Flow<DydxTransferSectionsView.ViewState?> =
        selectionFlow
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(selection: DydxTransferSectionsView.Selection): DydxTransferSectionsView.ViewState {
        return DydxTransferSectionsView.ViewState(
            localizer = localizer,
            selections = listOfNotNull(
                DydxTransferSectionsView.Selection.Deposit,
                DydxTransferSectionsView.Selection.Withdrawal,
                DydxTransferSectionsView.Selection.TransferOut,
                if (abacusStateManager.state.isMainNet != true) DydxTransferSectionsView.Selection.Faucet else null,
            ),
            currentSelection = selection,
            onSelectionChanged = { selection ->
                tracker.log(
                    event = "NavigateDialog",
                    data = mapOf(
                        "type" to when (selection) {
                            DydxTransferSectionsView.Selection.Deposit -> "Deposit2"
                            DydxTransferSectionsView.Selection.Withdrawal -> "Withdraw2"
                            DydxTransferSectionsView.Selection.TransferOut -> "Transfer"
                            DydxTransferSectionsView.Selection.Faucet -> "Faucet"
                        },
                    ),
                )
                selectionFlow.value = selection
            },
        )
    }
}
