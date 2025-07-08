package exchange.dydx.trading.feature.trade.tradeinput.components.sheettip

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.dydxstatemanager.selectedTypeText
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.AmountText
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.SizeTextView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import exchange.dydx.trading.feature.trade.tradeinput.DydxTradeInputView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@HiltViewModel
class DydxTradeSheetTipDraftViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val buttomSheetFlow: MutableStateFlow<@JvmSuppressWildcards DydxTradeInputView.BottomSheetState?>,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeSheetTipDraftView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput.filterNotNull(),
            abacusStateManager.state.configsAndAssetMap,
        ) { tradeInput, configsAndAssetMap ->
            if (tradeInput.marketId == null) {
                return@combine null
            }
            createViewState(tradeInput, configsAndAssetMap?.get(tradeInput.marketId))
        }
            .distinctUntilChanged()

    private fun createViewState(
        tradeInput: TradeInput,
        configsAndAsset: MarketConfigsAndAsset?,
    ): DydxTradeSheetTipDraftView.ViewState {
        val marketConfigs = configsAndAsset?.configs
        val asset = configsAndAsset?.asset

        val side = when (tradeInput.side) {
            OrderSide.Buy -> SideTextView.Side.Buy
            OrderSide.Sell -> SideTextView.Side.Sell
            else -> null
        }

        val symbol = asset?.displayableAssetId ?: configsAndAsset?.assetId

        val price = when (tradeInput.type) {
            OrderType.Limit, OrderType.StopLimit, OrderType.TakeProfitLimit -> tradeInput.price?.limitPrice
            OrderType.StopMarket, OrderType.TakeProfitMarket -> tradeInput.price?.triggerPrice
            OrderType.Market -> {
                val usdcSize = tradeInput.size?.usdcSize
                val size = tradeInput.size?.size
                if (usdcSize != null && size != null && size > 0.0) {
                    usdcSize / size
                } else {
                    null
                }
            }
            else -> null
        }

        return DydxTradeSheetTipDraftView.ViewState(
            localizer = localizer,
            type = tradeInput.selectedTypeText(localizer = localizer),
            side = if (side != null) {
                SideTextView.ViewState(
                    localizer = localizer,
                    side = side,
                    coloringOption = SideTextView.ColoringOption.WITH_BACKGROUND,
                )
            } else {
                null
            },
            size = if (tradeInput.size?.size != null) {
                SizeTextView.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    size = tradeInput.size?.size,
                    stepSize = marketConfigs?.displayStepSizeDecimals,
                )
            } else {
                null
            },
            token = if (symbol != null) {
                TokenTextView.ViewState(
                    symbol = symbol,
                )
            } else {
                null
            },
            price = if (price != null) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = price,
                    tickSize = marketConfigs?.displayTickSizeDecimals,
                )
            } else {
                null
            },
            onTapAction = {
                buttomSheetFlow.value = DydxTradeInputView.BottomSheetState.Expanded
            },
        )
    }
}
