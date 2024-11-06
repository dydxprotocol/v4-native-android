package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.size

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTradeInputSizeViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeInputSizeView.ViewState?> =
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
    ): DydxTradeInputSizeView.ViewState {
        return DydxTradeInputSizeView.ViewState(
            localizer = localizer,
            token = configsAndAsset?.asset?.displayableAssetId,
            size = tradeInput?.size?.size.let {
                formatter.raw(it, configsAndAsset?.configs?.displayStepSizeDecimals ?: 0)
            },
            usdcSize = tradeInput?.size?.usdcSize?.let {
                formatter.raw(it, 2)
            },
            placeholder = formatter.raw(0.0, configsAndAsset?.configs?.displayStepSizeDecimals ?: 0),
            onSizeChanged = { value ->
                abacusStateManager.trade(value, TradeInputField.size)
            },
            onUsdcSizeChanged = { value ->
                abacusStateManager.trade(value, TradeInputField.usdcSize)
            },
        )
    }
}
