package exchange.dydx.trading.feature.receipt.components.equity

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.AmountText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxReceiptEquityViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxReceiptEquityView.ViewState?> =
        abacusStateManager.state.selectedSubaccount
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        subaccount: Subaccount?
    ): DydxReceiptEquityView.ViewState {
        return DydxReceiptEquityView.ViewState(
            localizer = localizer,
            before = if (subaccount?.equity?.current != null) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = subaccount.equity?.current,
                    tickSize = 2,
                )
            } else {
                null
            },
            after = if (subaccount?.equity?.postOrder != null) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = subaccount.equity?.postOrder,
                    tickSize = 2,
                )
            } else {
                null
            },
        )
    }
}
