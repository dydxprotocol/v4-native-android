package exchange.dydx.trading.feature.trade.closeposition.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.ClosePositionInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.ClosePositionInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxClosePositionInputPercentViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    private data class Option(
        val percent: String,
        val value: Double,
    )

    private val options = listOf(
        Option("25%", 0.25),
        Option("50%", 0.5),
        Option("75%", 0.75),
        Option(localizer.localize("APP.TRADE.FULL_CLOSE"), 1.0),
    )

    val state: Flow<DydxClosePositionInputPercentView.ViewState?> =
        abacusStateManager.state.closePositionInput
            .map { input ->
                createViewState(input)
            }
            .distinctUntilChanged()

    private fun createViewState(
        closePositionInput: ClosePositionInput?,
    ): DydxClosePositionInputPercentView.ViewState {
        val index = options.indexOfFirst { it.value == closePositionInput?.size?.percent }
        return DydxClosePositionInputPercentView.ViewState(
            localizer = localizer,
            options = options.map { it.percent },
            selectedIndex = if (index == -1) null else index,
            onSelectionChanged = { index ->
                val percent = options.getOrNull(index)?.value
                if (percent != null) {
                    abacusStateManager.closePosition("$percent", ClosePositionInputField.percent)
                }
            },
        )
    }
}
