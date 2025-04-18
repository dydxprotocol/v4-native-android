package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.ordertype

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTradeInputOrderTypeViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeInputOrderTypeView.ViewState?> =
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
    ): DydxTradeInputOrderTypeView.ViewState {
        return DydxTradeInputOrderTypeView.ViewState(
            localizer = localizer,
            tokenLogoUrl = configsAndAsset?.asset?.resources?.imageUrl,
            orderTypes = tradeInput?.options?.typeOptions?.toList()?.mapNotNull {
                it.string ?: localizer.localize(it.stringKey ?: return@mapNotNull null)
            } ?: listOf(),
            selectedIndex = tradeInput?.options?.typeOptions?.indexOfFirst {
                it.type == tradeInput.type?.rawValue
            } ?: 0,
            onSelectionChanged = { index ->
                val type = tradeInput?.options?.typeOptions?.getOrNull(index)?.type
                if (type != null) {
                    abacusStateManager.trade(type, TradeInputField.type)
                }
            },
        )
    }
}
