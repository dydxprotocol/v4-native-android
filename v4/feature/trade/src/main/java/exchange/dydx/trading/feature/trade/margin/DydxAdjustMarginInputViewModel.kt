package exchange.dydx.trading.feature.trade.margin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.SubaccountPosition
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInput
import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType.Add
import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType.Remove
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.model.AdjustIsolatedMarginInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.maxLeverage
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxAdjustMarginInputViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    savedStateHandle: SavedStateHandle,
    private val parser: ParserProtocol,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    private val marketId: String? = savedStateHandle["marketId"]

    init {
        if (marketId == null) {
            router.navigateBack()
        } else {
            if (abacusStateManager.marketId.value != marketId) {
                abacusStateManager.setMarket(marketId = marketId)
                abacusStateManager.adjustIsolatedMargin(
                    data = null,
                    type = AdjustIsolatedMarginInputField.Amount,
                )
                abacusStateManager.adjustIsolatedMargin(
                    data = null,
                    type = AdjustIsolatedMarginInputField.AmountPercent,
                )
            }

            abacusStateManager.adjustIsolatedMargin(data = marketId, type = AdjustIsolatedMarginInputField.Market)

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
        combine(
            abacusStateManager.state.adjustMarginInput.filterNotNull(),
            marketId?.let { abacusStateManager.state.market(marketId) } ?: emptyFlow(),
            abacusStateManager.state.selectedSubaccountPositions
                .map { positions -> positions?.firstOrNull { it.id == marketId } }.filterNotNull(),
        ) { adjustMarginInput, market, position ->
            createViewState(adjustMarginInput, market.maxLeverage, position)
        }
            .distinctUntilChanged()

    private fun createViewState(
        adjustMarginInput: AdjustIsolatedMarginInput,
        marketMaxLeverage: Double,
        position: SubaccountPosition,
    ): DydxAdjustMarginInputView.ViewState {
        return DydxAdjustMarginInputView.ViewState(
            localizer = localizer,
            formatter = formatter,
            amountText = formatter.raw(adjustMarginInput.amount?.toDoubleOrNull(), 2),
            direction = when (adjustMarginInput.type) {
                Add -> DydxAdjustMarginInputView.MarginDirection.Add
                Remove -> DydxAdjustMarginInputView.MarginDirection.Remove
            },
            error = validate(adjustMarginInput, marketMaxLeverage, position),
            amountEditAction = { amount ->
                abacusStateManager.adjustIsolatedMargin(
                    data = amount,
                    type = AdjustIsolatedMarginInputField.Amount,
                )
            },
        )
    }

    private fun validate(
        input: AdjustIsolatedMarginInput,
        marketMaxLeverage: Double,
        position: SubaccountPosition,
    ): String? {
        val amount = input.amount ?: return null
        val summary = input.summary ?: return null
        val freeCollateral = position.freeCollateral.current
        val marginUsage = position.marginUsage.postOrder

        when (input.type) {
            Add -> {
                if (summary.crossFreeCollateral != null && amount.toDouble() >= summary.crossFreeCollateral!!) {
                    return localizer.localize("ERRORS.TRANSFER_MODAL.TRANSFER_MORE_THAN_FREE")
                }
                if (summary.crossMarginUsage != null && summary.crossMarginUsage!! > 1.0) {
                    return localizer.localize("ERRORS.TRADE_BOX.INVALID_NEW_ACCOUNT_MARGIN_USAGE")
                }
            }
            Remove -> {
                if (summary.positionLeverageUpdated != null && summary.positionLeverageUpdated!! > marketMaxLeverage) {
                    return localizer.localize("ERRORS.TRADE_BOX.POSITION_LEVERAGE_OVER_MAX")
                }
                if (marginUsage != null && marginUsage > 1) {
                    return localizer.localize("ERRORS.TRADE_BOX.INVALID_NEW_ACCOUNT_MARGIN_USAGE")
                }
                if (freeCollateral != null && amount.toDouble() > freeCollateral) {
                    return localizer.localize("ERRORS.TRANSFER_MODAL.TRANSFER_MORE_THAN_FREE")
                }
            }
        }
        return null
    }
}
