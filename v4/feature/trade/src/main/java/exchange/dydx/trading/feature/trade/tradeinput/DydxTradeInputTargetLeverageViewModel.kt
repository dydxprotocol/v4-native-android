package exchange.dydx.trading.feature.trade.tradeinput

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.PlatformInfo
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTradeInputTargetLeverageViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    val platformInfo: PlatformInfo,
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
                    }) ?: "2.0",
            leverageOptions = leverages,
            selectAction = { leverage ->
                this.targetLeverage.value = leverage
            },
            closeAction = {
                targetLeverage.let {
                    abacusStateManager.trade("$it", TradeInputField.targetLeverage)
                }
                router.navigateBack()
            },
        )
    }

    private fun leverageOptions(max: Double): List<LeverageTextAndValue> {
        val leverages = mutableListOf<LeverageTextAndValue>()
        if (max > 1.0) {
            leverages.add(LeverageTextAndValue("1.0", "1"))
        }
        if (max > 2.0) {
            leverages.add(LeverageTextAndValue("2.0", "2"))
        }
        if (max > 5.0) {
            leverages.add(LeverageTextAndValue("5.0", "5"))
        }
        if (max > 10.0) {
            leverages.add(LeverageTextAndValue("10.0", "10"))
        }
        leverages.add(LeverageTextAndValue(localizer.localize("APP.GENERAL.MAX"), "$max"))
        return leverages
    }
}
