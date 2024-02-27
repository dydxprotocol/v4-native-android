package exchange.dydx.trading.feature.trade.tradestatus.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.trade.streams.TradeStreaming
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTradeStatusHeaderViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val tradeStream: TradeStreaming,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTradeStatusHeaderView.ViewState?> =
        combine(
            tradeStream.submissionStatus,
            tradeStream.lastOrder,
            abacusStateManager.state.configsAndAssetMap,
        ) { submissionStatus, lastOrder, configsAndAssetMap ->
            createViewState(submissionStatus, lastOrder, configsAndAssetMap)
        }
            .distinctUntilChanged()

    private fun createViewState(
        submissionStatus: AbacusStateManagerProtocol.SubmissionStatus?,
        lastOrder: SubaccountOrder?,
        configsAndAssetMap: Map<String, MarketConfigsAndAsset>?,
    ): DydxTradeStatusHeaderView.ViewState {
        if (lastOrder != null && configsAndAssetMap != null) {
            val configsAndAsset = configsAndAssetMap?.get(lastOrder.marketId)
            val configs = configsAndAsset?.configs
            val asset = configsAndAsset?.asset
            if (configs != null && asset != null) {
                // state driven by the last order
                return DydxTradeStatusHeaderView.ViewState(
                    localizer = localizer,
                    status = when (lastOrder.status) {
                        OrderStatus.cancelled -> DydxTradeStatusHeaderView.StatusIcon.Failed
                        OrderStatus.canceling, OrderStatus.pending, OrderStatus.partiallyFilled -> DydxTradeStatusHeaderView.StatusIcon.Pending
                        OrderStatus.filled -> DydxTradeStatusHeaderView.StatusIcon.Filled
                        OrderStatus.open, OrderStatus.untriggered -> DydxTradeStatusHeaderView.StatusIcon.Open
                        else -> DydxTradeStatusHeaderView.StatusIcon.Submitting
                    },
                    title = localizer.localize(lastOrder?.resources?.statusStringKey ?: ""),
                    detail = when (lastOrder.status) {
                        OrderStatus.cancelled -> localizer.localize("APP.TRADE.ORDER_CANCELED_DESC")
                        OrderStatus.canceling, OrderStatus.pending, OrderStatus.partiallyFilled -> localizer.localize("APP.TRADE.ORDER_PENDING_DESC")
                        OrderStatus.filled -> localizer.localize("APP.TRADE.ORDER_FILLED_DESC")
                        OrderStatus.open -> localizer.localize("APP.TRADE.ORDER_PLACED_DESC")
                        OrderStatus.untriggered -> localizer.localize("APP.TRADE.NOT_TRIGGERED_STATUS_DESC")
                        else -> null
                    },
                    closeButtonAction = {
                        router.navigateBack()
                    },
                )
            }
        }
        // default state: driven by the submission status only.
        return DydxTradeStatusHeaderView.ViewState(
            localizer = localizer,
            status = when (submissionStatus) {
                is AbacusStateManagerProtocol.SubmissionStatus.Success -> DydxTradeStatusHeaderView.StatusIcon.Submitting
                is AbacusStateManagerProtocol.SubmissionStatus.Failed -> DydxTradeStatusHeaderView.StatusIcon.Failed
                else -> DydxTradeStatusHeaderView.StatusIcon.Submitting
            },
            title = when (submissionStatus) {
                is AbacusStateManagerProtocol.SubmissionStatus.Success -> localizer.localize("APP.TRADE.SUBMITTING_ORDER")
                is AbacusStateManagerProtocol.SubmissionStatus.Failed -> localizer.localize("APP.GENERAL.FAILED")
                else -> localizer.localize("APP.TRADE.SUBMITTING_ORDER")
            },
            detail = when (submissionStatus) {
                is AbacusStateManagerProtocol.SubmissionStatus.Success -> localizer.localize("APP.TRADE.SUBMITTING_ORDER_DESC")
                is AbacusStateManagerProtocol.SubmissionStatus.Failed -> submissionStatus.error?.message ?: submissionStatus.error.toString()
                else -> localizer.localize("APP.TRADE.SUBMITTING_ORDER_DESC")
            },
            closeButtonAction = {
                router.navigateBack()
            },
        )
    }
}
