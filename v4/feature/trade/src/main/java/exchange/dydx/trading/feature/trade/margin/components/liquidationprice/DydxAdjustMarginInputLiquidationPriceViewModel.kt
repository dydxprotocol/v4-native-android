package exchange.dydx.trading.feature.trade.margin.components.liquidationprice

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.SubaccountPosition
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
class DydxAdjustMarginInputLiquidationPriceViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxAdjustMarginInputLiquidationPriceView.ViewState?> =
        abacusStateManager.state.selectedSubaccountPositions
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        positions: List<SubaccountPosition>?,
    ): DydxAdjustMarginInputLiquidationPriceView.ViewState {
        val isolatedMargin = positions?.firstOrNull()

        return DydxAdjustMarginInputLiquidationPriceView.ViewState(
            localizer = localizer,
            before = AmountText.ViewState(
                localizer = localizer,
                formatter = formatter,
                amount = isolatedMargin?.liquidationPrice?.current,
                tickSize = 2,
            ),
            after = AmountText.ViewState(
                localizer = localizer,
                formatter = formatter,
                amount = isolatedMargin?.liquidationPrice?.postOrder,
                tickSize = 2,
            ),
        )
    }
}
