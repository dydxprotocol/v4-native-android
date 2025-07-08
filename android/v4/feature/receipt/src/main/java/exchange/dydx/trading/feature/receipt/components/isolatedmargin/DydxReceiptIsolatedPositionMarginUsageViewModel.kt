package exchange.dydx.trading.feature.receipt.components.isolatedmargin

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.SubaccountPendingPosition
import exchange.dydx.abacus.output.account.SubaccountPosition
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxReceiptIsolatedPositionMarginUsageViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: Flow<DydxReceiptIsolatedPositionMarginUsageView.ViewState?> =
        abacusStateManager.marketId
            .filterNotNull()
            .flatMapLatest { marketId ->
                combine(
                    abacusStateManager.state.selectedSubaccountPositionOfMarket(marketId),
                    abacusStateManager.state.selectedSubaccountPendingPositions.map { it?.firstOrNull { it.marketId == marketId } },
                ) { position, pendingPosition ->
                    createViewState(position, pendingPosition)
                }
            }
            .distinctUntilChanged()

    private fun createViewState(
        position: SubaccountPosition?,
        pendingPosition: SubaccountPendingPosition?,
    ): DydxReceiptIsolatedPositionMarginUsageView.ViewState {
        return DydxReceiptIsolatedPositionMarginUsageView.ViewState(
            localizer = localizer,
            formatter = formatter,
            before = position?.marginValue?.current ?: pendingPosition?.equity?.current,
            after = position?.marginValue?.postOrder ?: pendingPosition?.equity?.postOrder,
        )
    }
}
