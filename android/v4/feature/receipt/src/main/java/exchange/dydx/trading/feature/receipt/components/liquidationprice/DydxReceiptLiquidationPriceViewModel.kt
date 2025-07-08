package exchange.dydx.trading.feature.receipt.components.liquidationprice

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.SubaccountPosition
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.AmountText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class DydxReceiptLiquidationPriceViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxReceiptLiquidationPriceView.ViewState?> =
        combine(
            abacusStateManager.state.selectedSubaccountPositions,
            abacusStateManager.state.tradeInput.mapNotNull { it?.marketId },
            abacusStateManager.state.configsAndAssetMap.filterNotNull(),
        ) { positions, marketId, configsAndAssetMap ->
            val position = positions?.firstOrNull { it.id == marketId }
            val configAndAsset = configsAndAssetMap[marketId]
            createViewState(position, configAndAsset)
        }
            .distinctUntilChanged()

    private fun createViewState(
        position: SubaccountPosition?,
        configAndAsset: MarketConfigsAndAsset?,
    ): DydxReceiptLiquidationPriceView.ViewState {
        return DydxReceiptLiquidationPriceView.ViewState(
            localizer = localizer,
            before = if (position?.liquidationPrice?.current != null) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = position.liquidationPrice.current,
                    tickSize = configAndAsset?.configs?.tickSizeDecimals,
                    requiresPositive = true,
                )
            } else {
                null
            },
            after = if (position?.liquidationPrice?.postOrder != null) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = position.liquidationPrice.postOrder,
                    tickSize = configAndAsset?.configs?.tickSizeDecimals,
                    requiresPositive = true,
                )
            } else {
                null
            },
        )
    }
}
