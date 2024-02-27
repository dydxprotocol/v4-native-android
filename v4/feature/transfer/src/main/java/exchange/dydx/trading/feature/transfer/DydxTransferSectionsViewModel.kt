package exchange.dydx.trading.feature.transfer

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTransferSectionsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val selectionFlow: MutableStateFlow<DydxTransferSectionsView.Selection>
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTransferSectionsView.ViewState?> =
        selectionFlow
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(selection: DydxTransferSectionsView.Selection): DydxTransferSectionsView.ViewState {
        return DydxTransferSectionsView.ViewState(
            localizer = localizer,
            selections = listOf(
                DydxTransferSectionsView.Selection.Deposit,
                DydxTransferSectionsView.Selection.Withdrawal,
                DydxTransferSectionsView.Selection.TransferOut,
                if (abacusStateManager.state.isMainNet != true) DydxTransferSectionsView.Selection.Faucet else null,
            ).filterNotNull(),
            currentSelection = selection,
            onSelectionChanged = { selection ->
                selectionFlow.value = selection
            },
        )
    }
}
