package exchange.dydx.trading.feature.portfolio.cancelpendingposition

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.output.account.SubaccountPendingPosition
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.utils.filterNotNull
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.PlatformInfoViewModel
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.portfolio.cancelpendingposition.DydxCancelPendingPositionView.CtaButtonState
import exchange.dydx.trading.feature.receipt.components.buyingpower.DydxReceiptFreeCollateralView
import exchange.dydx.trading.feature.receipt.components.isolatedmargin.DydxReceiptIsolatedPositionMarginUsageView
import exchange.dydx.trading.feature.receipt.components.ordercount.DydxReceiptOrderCountView
import exchange.dydx.trading.feature.shared.views.AmountText
import exchange.dydx.trading.feature.shared.viewstate.SharedOrderViewState
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

private const val TAG = "DydxCancelPendingPositionViewModel"

@HiltViewModel
class DydxCancelPendingPositionViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    private val logger: Logging,
    savedStateHandle: SavedStateHandle,
    private val platformInfo: PlatformInfo,
) : ViewModel(), DydxViewModel {

    private var pendingOrdersFlow: MutableStateFlow<List<SubaccountOrder>> =
        MutableStateFlow(emptyList())

    private val marketId: String? = savedStateHandle["marketId"]

    val state: Flow<DydxCancelPendingPositionView.ViewState?> =
        combine(
            pendingOrdersFlow,
            abacusStateManager.state.selectedSubaccount,
            abacusStateManager.state.selectedSubaccountPendingPositions,
            abacusStateManager.state.marketMap,
            abacusStateManager.state.assetMap,
        ) { pendingOrders, selectedSubaccount, positions, marketMap, assetMap ->
            val pendingPosition = positions?.find { it.marketId == marketId } ?: return@combine null
            createViewState(
                pendingOrders = pendingOrders,
                selectedSubaccount = selectedSubaccount,
                pendingPosition = pendingPosition,
                marketMap = marketMap,
                assetMap = assetMap,
            )
        }
            .distinctUntilChanged()

    private fun createViewState(
        pendingOrders: List<SubaccountOrder>,
        selectedSubaccount: Subaccount?,
        pendingPosition: SubaccountPendingPosition,
        marketMap: Map<String, PerpetualMarket>?,
        assetMap: Map<String, Asset>?,
    ): DydxCancelPendingPositionView.ViewState {
        val asset = assetMap?.get(pendingPosition.assetId)

        val orderCountText = if (pendingPosition.orderCount == 1) {
            localizer.localize("APP.CANCEL_ORDERS_MODAL.ONE_OPEN_ORDER")
        } else {
            localizer.localizeWithParams(
                path = "APP.CANCEL_ORDERS_MODAL.N_OPEN_ORDERS",
                params = mapOf("COUNT" to "${pendingPosition.orderCount}").filterNotNull(),
            )
        }
        return DydxCancelPendingPositionView.ViewState(
            localizer = localizer,
            logoUrl = asset?.resources?.imageUrl,
            text = localizer.localizeWithParams(
                path = "APP.CANCEL_ORDERS_MODAL.CANCEL_ORDERS_CONFIRMATION",
                params = mapOf(
                    "OPEN_ORDERS_TEXT" to orderCountText,
                    "ASSET" to asset?.name,
                    "MARKET" to marketId,
                ).filterNotNull(),
            ),
            orderCount = DydxReceiptOrderCountView.ViewState(
                localizer = localizer,
                formatter = formatter,
                before = pendingPosition.orderCount,
                after = 0,
            ),
            marginUsage = DydxReceiptIsolatedPositionMarginUsageView.ViewState(
                localizer = localizer,
                formatter = formatter,
                before = pendingPosition.equity?.current,
                after = 0.0,
            ),
            freeCollateral = DydxReceiptFreeCollateralView.ViewState(
                localizer = localizer,
                label = localizer.localize("APP.GENERAL.CROSS_FREE_COLLATERAL"),
                before = AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = selectedSubaccount?.freeCollateral?.current,
                    tickSize = 2,
                ),
                after = AmountText.ViewState(
                    localizer = localizer,
                    formatter = formatter,
                    amount = selectedSubaccount?.freeCollateral?.current?.let {
                        pendingPosition.equity?.current?.let { equity ->
                            it + equity
                        }
                    },
                    tickSize = 2,
                ),
            ),
            closeAction = {
                router.navigateBack()
            },
            cancelAction = {
                cancelOrders(selectedSubaccount, marketMap, assetMap)
            },
            ctaButtonState = if (pendingOrders.isEmpty()) {
                CtaButtonState.Enabled
            } else {
                CtaButtonState.Disabled
            },
            ctaButtonTitle = if (pendingOrders.isNotEmpty()) {
                localizer.localize("APP.TRADE.CANCELING")
            } else {
                if (pendingPosition.orderCount == 1) {
                    localizer.localize("APP.TRADE.CANCEL_ORDER")
                } else {
                    localizer.localizeWithParams(
                        path = "APP.TRADE.CANCEL_ORDERS_COUNT",
                        params = mapOf("COUNT" to "${pendingPosition.orderCount}").filterNotNull(),
                    )
                }
            },
        )
    }

    private fun cancelOrders(
        selectedSubaccount: Subaccount?,
        marketMap: Map<String, PerpetualMarket>?,
        assetMap: Map<String, Asset>?,
    ) {
        pendingOrdersFlow.value = selectedSubaccount?.orders?.toList()?.filter {
            it.marketId == marketId && it.status == OrderStatus.Open || it.status == OrderStatus.Untriggered
        } ?: emptyList()

        processCancelOrders(marketMap, assetMap)
    }

    private fun processCancelOrders(
        marketMap: Map<String, PerpetualMarket>?,
        assetMap: Map<String, Asset>?,
    ) {
        if (pendingOrdersFlow.value.isNotEmpty()) {
            val ordersToCancel = pendingOrdersFlow.value.first()

            val sharedOrder = SharedOrderViewState.create(
                localizer = localizer,
                formatter = formatter,
                order = ordersToCancel,
                marketMap = marketMap,
                assetMap = assetMap,
            )
            abacusStateManager.cancelOrder(ordersToCancel.id) { result ->
                when (result) {
                    is AbacusStateManagerProtocol.SubmissionStatus.Success -> {
                        platformInfo.show(
                            message = localizer.localizeWithParams(
                                path = "APP.TRADE.CANCELING_ORDER_DESC",
                                params = mapOf(
                                    "SIDE" to ordersToCancel.side.rawValue,
                                    "SIZE" to (sharedOrder?.size ?: ""),
                                    "MARKET" to ordersToCancel.marketId,
                                ),
                            ),
                            type = PlatformInfoViewModel.Type.Info,
                            buttonTitle = localizer.localize("APP.GENERAL.OK"),
                            buttonAction = {
                                router.navigateBack()
                            },
                            duration = PlatformInfoViewModel.Duration.Indefinite,
                        )
                    }

                    is AbacusStateManagerProtocol.SubmissionStatus.Failed -> {
                        platformInfo.show(
                            message = result.error?.localizedString(localizer) ?: "",
                            type = PlatformInfoViewModel.Type.Error,
                        )
                    }

                    else -> {}
                }

                val pendingOrders = pendingOrdersFlow.value
                if (pendingOrders.isNotEmpty()) {
                    pendingOrdersFlow.value = pendingOrders.drop(1)
                }
                processCancelOrders(marketMap, assetMap)
            }
        } else {
            router.navigateBack()
        }
    }
}
