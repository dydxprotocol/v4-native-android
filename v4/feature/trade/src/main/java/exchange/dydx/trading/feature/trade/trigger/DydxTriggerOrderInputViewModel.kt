package exchange.dydx.trading.feature.trade.trigger

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxTriggerOrderInputViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val formatter: DydxFormatter,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), DydxViewModel {

    private val marketId: String?

    init {
        marketId = savedStateHandle["marketId"]

        if (marketId == null) {
            router.navigateBack()
        } else {
            abacusStateManager.setMarket(marketId = marketId)
            abacusStateManager.triggerOrders(input = marketId, type = TriggerOrdersInputField.marketId)
        }
    }

    val state: Flow<DydxTriggerOrderInputView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxTriggerOrderInputView.ViewState {
        return DydxTriggerOrderInputView.ViewState(
            localizer = localizer,
            closeAction = {
                router.navigateBack()
            },
        )
    }
}
