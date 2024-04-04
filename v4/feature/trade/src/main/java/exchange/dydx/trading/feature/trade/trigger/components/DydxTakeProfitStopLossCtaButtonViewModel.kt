package exchange.dydx.trading.feature.trade.trigger.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxTakeProfitStopLossCtaButtonViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTakeProfitStopLossCtaButtonView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxTakeProfitStopLossCtaButtonView.ViewState {
        return DydxTakeProfitStopLossCtaButtonView.ViewState(
            localizer = localizer,
            ctaButtonState = DydxTakeProfitStopLossCtaButtonView.State.Disabled(),
            ctaAction = {
                // TODO: Implement
            },
        )
    }
}
