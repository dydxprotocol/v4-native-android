package exchange.dydx.trading.feature.trade.marginmode

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.trade.tradeinput.DydxTradeInputView
import kotlinx.coroutines.CoroutineScope
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
class DydxTradeInputMarginModeViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
    private val abacusStateManager: AbacusStateManagerProtocol,
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

    val state: Flow<DydxTradeInputMarginModeView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput,
            marketAssetId,
            abacusStateManager.state.assetMap.filterNotNull(),
        ) { tradeInput, assetId, assetMap ->
            createViewState(tradeInput, assetId, assetMap)
        }
            .distinctUntilChanged()

    private fun createViewState(
        tradeInput: TradeInput?,
        assetId: String,
        assetMap: Map<String, Asset>,
    ): DydxTradeInputMarginModeView.ViewState {
        val marginMode = tradeInput?.marginMode ?: MarginMode.Cross
        return DydxTradeInputMarginModeView.ViewState(
            title = localizer.localize("APP.GENERAL.MARGIN_MODE"),
            asset = tradeInput?.marketId ?: "",
            crossMargin = DydxTradeInputMarginModeView.MarginTypeSelection(
                title = localizer.localize("APP.GENERAL.CROSS_MARGIN"),
                text = localizer.localize("APP.GENERAL.CROSS_MARGIN_DESCRIPTION"),
                selected = marginMode == MarginMode.Cross,
            ) {
                abacusStateManager.trade("CROSS", TradeInputField.marginMode)
                closeView()
            },
            isolatedMargin = DydxTradeInputMarginModeView.MarginTypeSelection(
                title = localizer.localize("APP.GENERAL.ISOLATED_MARGIN"),
                text = localizer.localize("APP.GENERAL.ISOLATED_MARGIN_DESCRIPTION"),
                selected = marginMode == MarginMode.Isolated,
            ) {
                abacusStateManager.trade("ISOLATED", TradeInputField.marginMode)
                closeView()
            },
            errorText = null,
            logoUrl = assetMap[assetId]?.resources?.imageUrl,
            closeAction = {
                closeView()
            },
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
