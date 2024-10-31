package exchange.dydx.trading.feature.portfolio.orderdetails

import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.canCancel
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.PlatformInfoViewModel
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import exchange.dydx.trading.feature.shared.viewstate.SharedFillViewState
import exchange.dydx.trading.feature.shared.viewstate.SharedOrderViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DydxOrderDetailsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    savedStateHandle: SavedStateHandle,
    val toaster: PlatformInfo,
) : ViewModel(), DydxViewModel {

    private val orderOrFillId: String? = savedStateHandle["id"]

    val state: Flow<DydxOrderDetailsView.ViewState?> =
        combine(
            abacusStateManager.state.selectedSubaccountFills,
            abacusStateManager.state.selectedSubaccountOrders,
            abacusStateManager.state.marketMap,
            abacusStateManager.state.assetMap,
        ) { fills, orders, marketMap, assetMap ->
            if (marketMap == null || assetMap == null) {
                return@combine null
            }
            val fill = fills?.firstOrNull { it.id == orderOrFillId }
            val order = orders?.firstOrNull { it.id == orderOrFillId }
            if (fill != null) {
                createFillViewState(fill, marketMap, assetMap)
            } else if (order != null) {
                createOrderViewState(order, marketMap, assetMap)
            } else {
                null
            }
        }
            .distinctUntilChanged()

    private fun createFillViewState(
        fill: SubaccountFill,
        marketMap: Map<String, PerpetualMarket>,
        assetMap: Map<String, Asset>,
    ): DydxOrderDetailsView.ViewState? {
        val sharedFillViewState = SharedFillViewState.create(
            localizer = localizer,
            formatter = formatter,
            fill = fill,
            marketMap = marketMap,
            assetMap = assetMap,
        ) ?: return null

        return DydxOrderDetailsView.ViewState(
            localizer = localizer,
            logoUrl = sharedFillViewState.logoUrl,
            side = SideTextView.ViewState(
                localizer = localizer,
                side = when (fill.side) {
                    OrderSide.Buy -> SideTextView.Side.Buy
                    OrderSide.Sell -> SideTextView.Side.Sell
                },
            ),
            closeAction = {
                router.navigateBack()
            },
            items = listOf(
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.MARKET"),
                    value = DydxOrderDetailsView.Item.ItemValue.Any {
                        TokenTextView.Content(
                            modifier = Modifier,
                            state = sharedFillViewState.token,
                        )
                    },
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.TYPE"),
                    value = DydxOrderDetailsView.Item.ItemValue.StringValue(
                        sharedFillViewState.type,
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.STATUS"),
                    value = DydxOrderDetailsView.Item.ItemValue.StringValue(
                        localizer.localize("APP.TRADE.ORDER_FILLED"),
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.TRADE.LIQUIDITY"),
                    value = DydxOrderDetailsView.Item.ItemValue.StringValue(
                        sharedFillViewState.feeLiquidity,
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.SIZE"),
                    value = DydxOrderDetailsView.Item.ItemValue.Number(
                        sharedFillViewState.size,
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.TRADE.AMOUNT_FILLED"),
                    value = DydxOrderDetailsView.Item.ItemValue.Number(
                        sharedFillViewState.size,
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.PRICE"),
                    value = DydxOrderDetailsView.Item.ItemValue.Number(
                        sharedFillViewState.price,
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.TRADE.FEE"),
                    value = DydxOrderDetailsView.Item.ItemValue.Number(
                        sharedFillViewState.fee,
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.CREATED_AT"),
                    value = DydxOrderDetailsView.Item.ItemValue.StringValue(
                        fill.createdAtMilliseconds.toLong().let {
                            formatter.dateTime(Instant.ofEpochMilli(it))
                        },
                    ),
                ),
            ),
        )
    }

    private fun createOrderViewState(
        order: SubaccountOrder,
        marketMap: Map<String, PerpetualMarket>,
        assetMap: Map<String, Asset>,
    ): DydxOrderDetailsView.ViewState? {
        val sharedOrderViewState = SharedOrderViewState.create(
            localizer = localizer,
            formatter = formatter,
            order = order,
            marketMap = marketMap,
            assetMap = assetMap,
        ) ?: return null
        return DydxOrderDetailsView.ViewState(
            localizer = localizer,
            logoUrl = sharedOrderViewState.logoUrl,
            side = SideTextView.ViewState(
                localizer = localizer,
                side = when (order.side) {
                    OrderSide.Buy -> SideTextView.Side.Buy
                    OrderSide.Sell -> SideTextView.Side.Sell
                },
            ),
            closeAction = {
                router.navigateBack()
            },
            cancelAction = if (order.status.canCancel) {
                {
                    cancelOrder(order, sharedOrderViewState)
                }
            } else {
                null
            },
            items = listOf(
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.MARKET"),
                    value = DydxOrderDetailsView.Item.ItemValue.Any {
                        TokenTextView.Content(
                            modifier = Modifier,
                            state = sharedOrderViewState.token,
                        )
                    },
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.TYPE"),
                    value = DydxOrderDetailsView.Item.ItemValue.StringValue(
                        sharedOrderViewState.type,
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.STATUS"),
                    value = DydxOrderDetailsView.Item.ItemValue.StringValue(
                        sharedOrderViewState.status,
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.SIZE"),
                    value = DydxOrderDetailsView.Item.ItemValue.Number(
                        sharedOrderViewState.size,
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.TRADE.AMOUNT_FILLED"),
                    value = DydxOrderDetailsView.Item.ItemValue.Number(
                        sharedOrderViewState.filledSize,
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.PRICE"),
                    value = DydxOrderDetailsView.Item.ItemValue.Number(
                        sharedOrderViewState.price,
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.TRADE.TRIGGER_PRICE"),
                    value = DydxOrderDetailsView.Item.ItemValue.Number(
                        sharedOrderViewState.triggerPrice,
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.CREATED_AT"),
                    value = DydxOrderDetailsView.Item.ItemValue.StringValue(
                        order.createdAtMilliseconds?.toLong()?.let {
                            formatter.dateTime(Instant.ofEpochMilli(it))
                        },
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.TRADE.GOOD_TIL_TIME"),
                    value = DydxOrderDetailsView.Item.ItemValue.StringValue(
                        order.expiresAtMilliseconds?.toLong()?.let {
                            formatter.dateTime(Instant.ofEpochMilli(it))
                        },
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.TRADE.GOOD_TIL"),
                    value = DydxOrderDetailsView.Item.ItemValue.StringValue(
                        order.goodTilBlock?.toLong()?.let {
                            "$it"
                        },
                    ),
                ),
                DydxOrderDetailsView.Item(
                    title = localizer.localize("APP.TRADE.TIME_IN_FORCE"),
                    value = DydxOrderDetailsView.Item.ItemValue.StringValue(
                        order.resources.timeInForceString
                            ?: order.resources.timeInForceStringKey?.let {
                                localizer.localize(
                                    it,
                                )
                            },
                    ),
                ),
                if (order.cancelReason != null) {
                    DydxOrderDetailsView.Item(
                        title = localizer.localize("APP.TRADE.CANCEL_REASON"),
                        value = DydxOrderDetailsView.Item.ItemValue.StringValue(
                            order.cancelReason?.let {
                                localizer.localize(
                                    "APP.TRADE.$it",
                                )
                            },
                        ),
                    )
                } else {
                    null
                },
            ).filterNotNull(),
        )
    }

    private fun cancelOrder(order: SubaccountOrder, sharedOrderViewState: SharedOrderViewState) {
        abacusStateManager.cancelOrder(order.id) { result ->
            when (result) {
                is AbacusStateManagerProtocol.SubmissionStatus.Success -> {
                    toaster.show(
                        message = localizer.localizeWithParams(
                            path = "APP.TRADE.CANCELING_ORDER_DESC",
                            params = mapOf(
                                "SIDE" to order.side.rawValue,
                                "SIZE" to (sharedOrderViewState.size ?: ""),
                                "MARKET" to order.marketId,
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
                    toaster.show(
                        message = result.error?.localizedString(localizer) ?: "",
                        type = PlatformInfoViewModel.Type.Error,
                    )
                }
                else -> {}
            }
        }
    }
}
