package exchange.dydx.trading.feature.trade.margin.components.type

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInput
import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.AdjustIsolatedMarginInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.trade.margin.DydxAdjustMarginInputView.MarginDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
@HiltViewModel
class DydxAdjustMarginInputTypeViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxAdjustMarginInputTypeView.ViewState?> =
        abacusStateManager.state.adjustMarginInput.filterNotNull()
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        adjustMarginInput: AdjustIsolatedMarginInput
    ): DydxAdjustMarginInputTypeView.ViewState {
        return DydxAdjustMarginInputTypeView.ViewState(
            localizer = localizer,
            direction = when (adjustMarginInput.type) {
                IsolatedMarginAdjustmentType.Add -> MarginDirection.Add
                IsolatedMarginAdjustmentType.Remove -> MarginDirection.Remove
            },
            marginDirectionAction = { direction ->
                abacusStateManager.adjustIsolatedMargin(
                    data = when (direction) {
                        MarginDirection.Add -> IsolatedMarginAdjustmentType.Add
                        MarginDirection.Remove -> IsolatedMarginAdjustmentType.Remove
                    }.name,
                    type = AdjustIsolatedMarginInputField.Type,
                )
            },
        )
    }
}
