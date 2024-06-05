package exchange.dydx.trading.feature.trade.targetleverage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.trade.tradeinput.DydxTradeInputView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class DydxTradeInputTargetLeverageViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val buttomSheetStateFlow: MutableStateFlow<@JvmSuppressWildcards DydxTradeInputView.BottomSheetState?>,
    @CoroutineScopes.App private val appScope: CoroutineScope,
) : ViewModel(), DydxViewModel {
    var targetLeverage: MutableStateFlow<String?> = MutableStateFlow(null)

    val state: Flow<DydxTradeInputTargetLeverageView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput,
            targetLeverage,
        ) { selectedSubaccountPosition, marketAndAsset ->
            createViewState(selectedSubaccountPosition, marketAndAsset)
        }
            .distinctUntilChanged()

    private fun createViewState(
        tradeInput: TradeInput?,
        targetLeverage: String?
    ): DydxTradeInputTargetLeverageView.ViewState {
        val maxLeverage = tradeInput?.options?.maxLeverage ?: 5.0
        val leverages = leverageOptions(maxLeverage)
        return DydxTradeInputTargetLeverageView.ViewState(
            localizer = localizer,
            leverageText = targetLeverage ?: (
                tradeInput?.targetLeverage?.let {
                    "$it"
                }
                ) ?: "2.0",
            leverageOptions = leverages,
            selectAction = { leverage ->
                this.targetLeverage.value = leverage
            },
            closeAction = {
                targetLeverage.let {
                    abacusStateManager.trade("$it", TradeInputField.targetLeverage)
                }
                closeView()
            },
        )
    }

    private fun leverageOptions(max: Double): List<LeverageTextAndValue> {
        val steps = listOf(1.0, 2.0, 5.0, 10.0)
        val leverages = mutableListOf<LeverageTextAndValue>()
        for (step in steps) {
            if (max > step) {
                leverages.add(leverageTextAndValue(step))
            }
        }
        leverages.add(
            LeverageTextAndValue(
                localizer.localize("APP.GENERAL.MAX"),
                formatter.raw(max) ?: "",
            ),
        )
        return leverages
    }

    private fun leverageTextAndValue(value: Double): LeverageTextAndValue {
        return LeverageTextAndValue(
            formatter.leverage(value, 1) ?: "",
            formatter.raw(value) ?: "",
        )
    }

    private fun closeView() {
        router.navigateBack()
        appScope.launch {
            delay(1.seconds) // Delay to allow the back navigation to complete
            buttomSheetStateFlow.value = DydxTradeInputView.BottomSheetState.Expanded
        }
    }
}
