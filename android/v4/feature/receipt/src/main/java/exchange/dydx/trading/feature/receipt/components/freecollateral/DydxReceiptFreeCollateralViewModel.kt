package exchange.dydx.trading.feature.receipt.components.buyingpower

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.SubaccountPendingPosition
import exchange.dydx.abacus.output.account.SubaccountPosition
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.AmountText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxReceiptFreeCollateralViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: Flow<DydxReceiptFreeCollateralView.ViewState?> =
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
        pendingPosition: SubaccountPendingPosition?
    ): DydxReceiptFreeCollateralView.ViewState {
        val current = position?.freeCollateral?.current ?: pendingPosition?.freeCollateral?.current
        val postOrder = position?.freeCollateral?.postOrder ?: pendingPosition?.freeCollateral?.postOrder
        return DydxReceiptFreeCollateralView.ViewState(
            localizer = localizer,
            before = if (current != null) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = current,
                    tickSize = 0,
                    requiresPositive = true,
                )
            } else {
                null
            },
            after = if (postOrder != null) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = postOrder,
                    tickSize = 0,
                    requiresPositive = true,
                )
            } else {
                null
            },
        )
    }
}
