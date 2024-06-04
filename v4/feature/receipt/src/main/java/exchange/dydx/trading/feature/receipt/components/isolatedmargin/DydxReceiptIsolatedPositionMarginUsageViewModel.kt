package exchange.dydx.trading.feature.receipt.components.isolatedmargin

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.TradeStatesWithDoubleValues
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxReceiptIsolatedPositionMarginUsageViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxReceiptIsolatedPositionMarginUsageView.ViewState?> =
        abacusStateManager.marketId
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        marketId: String?
    ): DydxReceiptIsolatedPositionMarginUsageView.ViewState {
        val marginUsage: TradeStatesWithDoubleValues? =
            null
        /*
        TODO: After abacus exposes marginUsage, changes to next line
        if (marketId != null) abacusStateManager.state.selectedSubaccountPositionOfMarket(marketId).value?.leverage else null
         */
        return DydxReceiptIsolatedPositionMarginUsageView.ViewState(
            localizer = localizer,
            formatter = formatter,
            before = marginUsage?.current,
            after = marginUsage?.postOrder,
        )
    }
}
