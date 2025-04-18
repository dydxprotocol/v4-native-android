package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.triggerprice

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LabeledTextInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTradeInputTriggerPriceViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeInputTriggerPriceView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput,
            abacusStateManager.state.configsAndAssetMap,
        ) { tradeInput, configsAndAssetMap ->
            val marketId = tradeInput?.marketId ?: return@combine null
            createViewState(tradeInput, configsAndAssetMap?.get(marketId))
        }
            .distinctUntilChanged()

    private fun createViewState(
        tradeInput: TradeInput?,
        configsAndAsset: MarketConfigsAndAsset?,
    ): DydxTradeInputTriggerPriceView.ViewState {
        val marketConfigs = configsAndAsset?.configs
        return DydxTradeInputTriggerPriceView.ViewState(
            localizer = localizer,
            labeledTextInput = LabeledTextInput.ViewState(
                localizer = localizer,
                label = localizer.localize("APP.TRADE.TRIGGER_PRICE"),
                token = "USD",
                value = tradeInput?.price?.triggerPrice?.let {
                    formatter.raw(it, marketConfigs?.displayTickSizeDecimals ?: 0)
                },
                placeholder = formatter.raw(0.0, marketConfigs?.displayTickSizeDecimals ?: 0),
                onValueChanged = { value ->
                    abacusStateManager.trade(value, TradeInputField.triggerPrice)
                },
            ),
        )
    }
}
