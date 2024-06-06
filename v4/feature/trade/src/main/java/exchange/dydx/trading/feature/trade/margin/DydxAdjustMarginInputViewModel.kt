package exchange.dydx.trading.feature.trade.margin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInput
import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.model.AdjustIsolatedMarginInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxAdjustMarginInputViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    savedStateHandle: SavedStateHandle,
    private val parser: ParserProtocol,
) : ViewModel(), DydxViewModel {

    private val marketId: String? = savedStateHandle["marketId"]

    init {
        if (marketId == null) {
            router.navigateBack()
        } else {
            abacusStateManager.setMarket(marketId = marketId)

            abacusStateManager.state.selectedSubaccountPositions.value?.firstOrNull {
                it.id == marketId
            }?.let {
                abacusStateManager.adjustIsolatedMargin(
                    data = parser.asString(it.childSubaccountNumber),
                    type = AdjustIsolatedMarginInputField.ChildSubaccountNumber,
                )
            }
        }
    }

    val state: Flow<DydxAdjustMarginInputView.ViewState?> =
        abacusStateManager.state.adjustMarginInput.filterNotNull()
            .map { adjustMarginInput ->
                createViewState(adjustMarginInput)
            }
            .distinctUntilChanged()

    private fun createViewState(
        adjustMarginInput: AdjustIsolatedMarginInput,
    ): DydxAdjustMarginInputView.ViewState {
        return DydxAdjustMarginInputView.ViewState(
            localizer = localizer,
            formatter = formatter,
            amountText = formatter.raw(adjustMarginInput.amount?.toDoubleOrNull(), 2),
            direction = when (adjustMarginInput.type) {
                IsolatedMarginAdjustmentType.Add -> DydxAdjustMarginInputView.MarginDirection.Add
                IsolatedMarginAdjustmentType.Remove -> DydxAdjustMarginInputView.MarginDirection.Remove
            },
            error = null,
            amountEditAction = { amount ->
                abacusStateManager.adjustIsolatedMargin(
                    data = amount,
                    type = AdjustIsolatedMarginInputField.Amount,
                )
            },
        )
    }
}
