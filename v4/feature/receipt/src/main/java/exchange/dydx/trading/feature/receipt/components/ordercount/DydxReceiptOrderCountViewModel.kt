package exchange.dydx.trading.feature.receipt.components.ordercount

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxReceiptOrderCountViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxReceiptOrderCountView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxReceiptOrderCountView.ViewState {
        return DydxReceiptOrderCountView.ViewState(
            localizer = localizer,
            formatter = formatter,
        )
    }
}
