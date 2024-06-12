package exchange.dydx.trading.feature.trade.margin.components.liquidationprice

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.platformui.components.gradient.GradientType
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.AmountText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@HiltViewModel
class DydxAdjustMarginInputLiquidationPriceViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxAdjustMarginInputLiquidationPriceView.ViewState?> =
        combine(
            abacusStateManager.state.adjustMarginInput.filterNotNull(),
            abacusStateManager.state.configsAndAssetMap,
        ) { adjustMarginInput, configsAndAssetMap ->
            createViewState(adjustMarginInput, configsAndAssetMap)
        }
            .distinctUntilChanged()

    private fun createViewState(
        adjustMarginInput: AdjustIsolatedMarginInput,
        configsAndAssetMap: Map<String, MarketConfigsAndAsset>?,
    ): DydxAdjustMarginInputLiquidationPriceView.ViewState {
        val configsAndAsset = abacusStateManager.marketId.value?.let {
            configsAndAssetMap?.get(it)
        }
        return DydxAdjustMarginInputLiquidationPriceView.ViewState(
            localizer = localizer,
            before = AmountText.ViewState(
                localizer = localizer,
                formatter = formatter,
                amount = adjustMarginInput.summary?.liquidationPrice,
                tickSize = configsAndAsset?.configs?.tickSizeDecimals,
            ),
            after = adjustMarginInput.summary?.liquidationPriceUpdated?.let {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = it,
                    tickSize = configsAndAsset?.configs?.tickSizeDecimals,
                )
            },
            direction = adjustMarginInput.summary?.liquidationPrice?.let { liquidationPrice ->
                adjustMarginInput.summary?.liquidationPriceUpdated?.let { liquidationPriceUpdated ->
                    if (liquidationPrice < liquidationPriceUpdated) {
                        GradientType.PLUS
                    } else {
                        GradientType.MINUS
                    }
                }
            } ?: GradientType.NONE,
        )
    }
}
