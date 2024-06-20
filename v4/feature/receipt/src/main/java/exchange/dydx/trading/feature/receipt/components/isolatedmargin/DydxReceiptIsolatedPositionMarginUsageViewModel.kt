package exchange.dydx.trading.feature.receipt.components.isolatedmargin

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.SubaccountPendingPosition
import exchange.dydx.abacus.output.SubaccountPosition
import exchange.dydx.abacus.output.TradeStatesWithDoubleValues
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
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
            before = positionMarginCurrent(
                marginMode = position?.marginMode,
                equity = position?.equity,
                notionalTotal = position?.notionalTotal,
                maintenanceMarginFraction = position?.adjustedMmf?.current,
            ) ?: positionMarginCurrent(
                marginMode = MarginMode.Isolated,
                equity = pendingPosition?.equity,
                notionalTotal = null,
                maintenanceMarginFraction = null,
            ),
            after = positionMarginPostOrder(
                marginMode = position?.marginMode,
                equity = position?.equity,
                notionalTotal = position?.notionalTotal,
                maintenanceMarginFraction = position?.adjustedMmf?.postOrder,
            ) ?: positionMarginPostOrder(
                marginMode = MarginMode.Isolated,
                equity = pendingPosition?.equity,
                notionalTotal = null,
                maintenanceMarginFraction = null,
            ),
        )
    }

    private fun positionMarginCurrent(
        marginMode: MarginMode?,
        equity: TradeStatesWithDoubleValues?,
        notionalTotal: TradeStatesWithDoubleValues?,
        maintenanceMarginFraction: Double?
    ): Double? {
        return when (marginMode) {
            MarginMode.Cross -> return notionalTotal?.current?.let { notionalTotal ->
                maintenanceMarginFraction?.let { maintenanceMarginFraction ->
                    maintenanceMarginFraction * notionalTotal
                }
            }
            MarginMode.Isolated -> equity?.current
            else -> null
        }
    }

    private fun positionMarginPostOrder(
        marginMode: MarginMode?,
        equity: TradeStatesWithDoubleValues?,
        notionalTotal: TradeStatesWithDoubleValues?,
        maintenanceMarginFraction: Double?
    ): Double? {
        return when (marginMode) {
            MarginMode.Cross -> return notionalTotal?.postOrder?.let { notionalTotal ->
                maintenanceMarginFraction?.let { maintenanceMarginFraction ->
                    maintenanceMarginFraction * notionalTotal
                }
            }
            MarginMode.Isolated -> equity?.postOrder
            else -> null
        }
    }
}
