package exchange.dydx.trading.feature.trade.closeposition.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.ClosePositionInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxClosePositionInputSizeViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxClosePositionInputSizeView.ViewState?> =
        combine(
            abacusStateManager.state.closePositionInput,
            abacusStateManager.state.configsAndAssetMap,
        ) { input, configsAndAssetMap ->
            val marketId = input?.marketId ?: return@combine null
            createViewState(input, configsAndAssetMap?.get(marketId))
        }
            .distinctUntilChanged()

    private fun createViewState(
        input: ClosePositionInput?,
        configsAndAsset: MarketConfigsAndAsset?,
    ): DydxClosePositionInputSizeView.ViewState {
        return DydxClosePositionInputSizeView.ViewState(
            localizer = localizer,
            token = configsAndAsset?.asset?.displayableAssetId,
            size = input?.size?.size.let {
                formatter.raw(it, configsAndAsset?.configs?.displayStepSizeDecimals ?: 0)
            },
            placeholder = formatter.raw(0.0, configsAndAsset?.configs?.displayStepSizeDecimals ?: 0),
            onSizeChanged = { value ->
                abacusStateManager.closePosition(value, ClosePositionInputField.size)
            },
        )
    }
}
