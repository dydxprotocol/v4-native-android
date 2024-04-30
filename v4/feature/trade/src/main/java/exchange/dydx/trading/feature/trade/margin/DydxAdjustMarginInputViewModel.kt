package exchange.dydx.trading.feature.trade.margin

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.PlatformInfo
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxAdjustMarginInputViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    val platformInfo: PlatformInfo,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxAdjustMarginInputView.ViewState?> = abacusStateManager.state.tradeInput
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(tradeInput: TradeInput?): DydxAdjustMarginInputView.ViewState {
        /*
        Abacus not implemented for adjust margin yet. This is a placeholder.
         */
        return DydxAdjustMarginInputView.ViewState(
            localizer = localizer,
            formatter = formatter,
            direction = DydxAdjustMarginInputView.MarginDirection.Add,
            percentage = 0.5,
            percentageOptions = listOf(
                DydxAdjustMarginInputView.PercentageOption("10%", 0.1),
                DydxAdjustMarginInputView.PercentageOption("20%", 0.2),
                DydxAdjustMarginInputView.PercentageOption("30%", 0.3),
                DydxAdjustMarginInputView.PercentageOption("50%", 0.5),
            ),
            amountText = "500",
            subaccountReceipt = DydxAdjustMarginInputView.SubaccountReceipt(
                freeCollateral = listOf("1000.00", "500.00"),
                marginUsage = listOf("19.34", "38.45"),
            ),
            positionReceipt = DydxAdjustMarginInputView.PositionReceipt(
                freeCollateral = listOf("1000.00", "1500.00"),
                leverage = listOf("3.1", "2.4"),
                liquidationPrice = listOf("1200.00", "1000.00"),
            ),
            error = null,
            marginDirectionAction = { },
            percentageAction = { },
            editAction = { },
            action = { },
            closeAction = {
                router.navigateBack()
            },
        )
    }
}
