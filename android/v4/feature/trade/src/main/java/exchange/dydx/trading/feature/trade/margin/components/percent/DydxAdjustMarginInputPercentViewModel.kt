package exchange.dydx.trading.feature.trade.margin.components.percent

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.machine.AdjustIsolatedMarginInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.trade.margin.DydxAdjustMarginInputView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxAdjustMarginInputPercentViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val parser: ParserProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxAdjustMarginInputPercentView.ViewState?> =
        abacusStateManager.state.adjustMarginInput.filterNotNull()
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        adjustMarginInput: AdjustIsolatedMarginInput
    ): DydxAdjustMarginInputPercentView.ViewState {
        return DydxAdjustMarginInputPercentView.ViewState(
            localizer = localizer,
            percentage = adjustMarginInput.amountPercent?.toDoubleOrNull(),
            percentageOptions = listOf(
                DydxAdjustMarginInputView.PercentageOption("5%", 0.05),
                DydxAdjustMarginInputView.PercentageOption("10%", 0.1),
                DydxAdjustMarginInputView.PercentageOption("25%", 0.25),
                DydxAdjustMarginInputView.PercentageOption("50%", 0.5),
                DydxAdjustMarginInputView.PercentageOption("75%", 0.75),
            ),
            onPercentageChanged = { percentage ->
                abacusStateManager.adjustIsolatedMargin(
                    data = parser.asString(percentage),
                    type = AdjustIsolatedMarginInputField.AmountPercent,
                )
            },
        )
    }
}
