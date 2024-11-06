package exchange.dydx.trading.feature.trade.tradestatus.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.output.account.SubaccountPosition
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.AmountText
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.SizeTextView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import exchange.dydx.trading.feature.trade.streams.TradeStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTradeStatusPositionViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val tradeStream: TradeStreaming,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeStatusPositionView.ViewState?> =
        combine(
            abacusStateManager.state.tradeInput,
            abacusStateManager.state.configsAndAssetMap,
            abacusStateManager.state.selectedSubaccountPositions,
            tradeStream.lastOrder,
        ) { tradeInput, configsAndAssetMap, selectedSubaccountPositions, lastOrder ->
            val tradeInput = tradeInput ?: return@combine null
            val marketId = tradeInput.marketId ?: return@combine null
            val configsAndAsset = configsAndAssetMap?.get(marketId) ?: return@combine null
            val position = selectedSubaccountPositions?.firstOrNull { it.id == marketId } ?: return@combine null

            createViewState(configsAndAsset, position, lastOrder)
        }
            .distinctUntilChanged()

    private fun createViewState(
        configsAndAsset: MarketConfigsAndAsset,
        position: SubaccountPosition?,
        lastOrder: SubaccountOrder?,
    ): DydxTradeStatusPositionView.ViewState {
        val hidePostOrderValues = lastOrder?.status != null
        return DydxTradeStatusPositionView.ViewState(
            localizer = localizer,
            tokenUrl = configsAndAsset.asset?.resources?.imageUrl,
            token = if (configsAndAsset.asset?.displayableAssetId?.isNotEmpty() == true) {
                TokenTextView.ViewState(
                    symbol = configsAndAsset.asset?.displayableAssetId ?: "",
                )
            } else {
                null
            },
            sideBefore = createSideTextViewState(position?.side?.current),
            sideAfter = if (!hidePostOrderValues) createSideTextViewState(position?.side?.postOrder) else null,
            sizeBefore = SizeTextView.ViewState(
                localizer = localizer,
                formatter = formatter,
                size = position?.size?.current ?: 0.0,
                stepSize = configsAndAsset.configs?.stepSizeDecimals,
            ),
            sizeAfter = if (!hidePostOrderValues) {
                SizeTextView.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    size = position?.size?.postOrder ?: 0.0,
                    stepSize = configsAndAsset.configs?.stepSizeDecimals,
                )
            } else {
                null
            },
            valueBefore = AmountText.ViewState(
                localizer = localizer,
                formatter = formatter,
                amount = position?.valueTotal?.current ?: 0.0,
                tickSize = null,
            ),
            valueAfter = if (!hidePostOrderValues) {
                AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = position?.valueTotal?.postOrder ?: 0.0,
                    tickSize = null,
                )
            } else {
                null
            },
        )
    }

    private fun createSideTextViewState(
        side: PositionSide?,
    ): SideTextView.ViewState {
        when (side) {
            PositionSide.LONG -> {
                return SideTextView.ViewState(
                    localizer = localizer,
                    coloringOption = SideTextView.ColoringOption.WITH_BACKGROUND,
                    side = SideTextView.Side.Long,
                )
            }
            PositionSide.SHORT -> {
                return SideTextView.ViewState(
                    localizer = localizer,
                    coloringOption = SideTextView.ColoringOption.WITH_BACKGROUND,
                    side = SideTextView.Side.Short,
                )
            }
            else -> {
                return SideTextView.ViewState(
                    localizer = localizer,
                    coloringOption = SideTextView.ColoringOption.WITH_BACKGROUND,
                    side = SideTextView.Side.None,
                )
            }
        }
    }
}
