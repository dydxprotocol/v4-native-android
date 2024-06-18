package exchange.dydx.trading.feature.trade.closeposition

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.ClosePositionInput
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxClosePositionInputViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), DydxViewModel {

    private val marketId: String? = savedStateHandle["marketId"]

    init {

        if (marketId == null) {
            router.navigateBack()
        } else {
            abacusStateManager.setMarket(marketId = marketId)
            abacusStateManager.startClosePosition(marketId = marketId)
        }
    }

    val state: Flow<DydxClosePositionInputView.ViewState?> =
        abacusStateManager.state.closePositionInput
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(closePositionInput: ClosePositionInput?): DydxClosePositionInputView.ViewState {
        return DydxClosePositionInputView.ViewState(
            localizer = localizer,
            side = when (closePositionInput?.side) {
                OrderSide.Sell -> DydxClosePositionInputView.DisplaySide.Bids
                OrderSide.Buy -> DydxClosePositionInputView.DisplaySide.Asks
                else -> DydxClosePositionInputView.DisplaySide.None
            },
        )
    }
}
