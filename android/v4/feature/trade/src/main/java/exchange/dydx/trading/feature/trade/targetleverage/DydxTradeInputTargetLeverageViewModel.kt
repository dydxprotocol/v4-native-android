package exchange.dydx.trading.feature.trade.targetleverage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.maxLeverage
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.trade.tradeinput.DydxTradeInputView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class DydxTradeInputTargetLeverageViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val parser: ParserProtocol,
    private val router: DydxRouter,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val buttomSheetStateFlow: MutableStateFlow<@JvmSuppressWildcards DydxTradeInputView.BottomSheetState?>,
    @CoroutineScopes.App private val appScope: CoroutineScope,
) : ViewModel(), DydxViewModel {

    private val marketAssetId: Flow<String> =
        abacusStateManager.state.tradeInput
            .mapNotNull { it?.marketId }
            .flatMapLatest { marketId ->
                abacusStateManager.state.market(marketId)
            }
            .mapNotNull { it?.assetId }
            .distinctUntilChanged()

    private var targetLeverage: MutableStateFlow<String?> = MutableStateFlow(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: Flow<DydxTradeInputTargetLeverageView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput,
            abacusStateManager.state.tradeInput.mapNotNull { it?.marketId }.flatMapLatest { abacusStateManager.state.market(it) }.mapNotNull { it?.maxLeverage },
            targetLeverage,
            marketAssetId,
            abacusStateManager.state.assetMap.filterNotNull(),
        ) { selectedSubaccountPosition, maxLeverage, leverage, assetId, assetMap ->
            createViewState(selectedSubaccountPosition, maxLeverage, leverage, assetId, assetMap)
        }
            .distinctUntilChanged()

    private fun createViewState(
        tradeInput: TradeInput?,
        maxLeverage: Double,
        targetLeverage: String?,
        assetId: String,
        assetMap: Map<String, Asset>,
    ): DydxTradeInputTargetLeverageView.ViewState {
        val leverages = leverageOptions(maxLeverage)
        return DydxTradeInputTargetLeverageView.ViewState(
            localizer = localizer,
            parser = parser,
            leverageText = targetLeverage ?: formatter.raw(tradeInput?.targetLeverage, 2),
            leverageOptions = leverages,
            logoUrl = assetMap[assetId]?.resources?.imageUrl,
            selectAction = { leverage ->
                this.targetLeverage.value = leverage
            },
            closeAction = {
                closeView()
            },
            ctaButtonAction = {
                targetLeverage.let {
                    abacusStateManager.trade("$it", TradeInputField.targetLeverage)
                }
                closeView()
            },
        )
    }

    private fun leverageOptions(max: Double): List<LeverageTextAndValue> {
        val steps = listOf(1.0, 2.0, 3.0, 5.0, 10.0)
        val leverages = mutableListOf<LeverageTextAndValue>()
        for (step in steps) {
            if (max > step) {
                leverages.add(leverageTextAndValue(step))
            }
        }
        leverages.add(
            LeverageTextAndValue(
                text = localizer.localize("APP.GENERAL.MAX"),
                value = formatter.raw(max) ?: "",
            ),
        )
        return leverages
    }

    private fun leverageTextAndValue(value: Double): LeverageTextAndValue {
        return LeverageTextAndValue(
            text = formatter.leverage(value, 1) ?: "",
            value = formatter.raw(value) ?: "",
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
