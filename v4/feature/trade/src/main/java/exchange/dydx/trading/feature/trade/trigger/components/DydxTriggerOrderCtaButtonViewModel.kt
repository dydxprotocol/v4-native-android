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
class DydxTriggerOrderCtaButtonViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTriggerOrderCtaButtonView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxTriggerOrderCtaButtonView.ViewState {
        return DydxTriggerOrderCtaButtonView.ViewState(
            localizer = localizer,
            ctaButtonState = DydxTriggerOrderCtaButtonView.State.Disabled(),
            ctaAction = {
                // TODO: Implement
            },
        )
    }
}