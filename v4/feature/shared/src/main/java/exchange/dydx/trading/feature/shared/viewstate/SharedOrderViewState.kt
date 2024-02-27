package exchange.dydx.trading.feature.shared.viewstate

import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.views.IntervalText
import exchange.dydx.trading.feature.shared.views.OrderStatusView
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import java.time.Instant

data class SharedOrderViewState(
    val localizer: LocalizerProtocol,
    val id: String,
    val date: IntervalText.ViewState? = null,
    val status: String? = null,
    val canCancel: Boolean = false,
    val orderStatus: OrderStatusView.ViewState? = null,
    val sideText: SideTextView.ViewState? = null,
    val type: String? = null,
    val size: String? = null,
    val filledSize: String? = null,
    val price: String? = null,
    val triggerPrice: String? = null,
    val token: TokenTextView.ViewState? = null,
    val logoUrl: String? = null,
) {
    companion object {
        val preview = SharedOrderViewState(
            localizer = MockLocalizer(),
            id = "id",
            date = IntervalText.ViewState.preview,
            status = "Open",
            orderStatus = OrderStatusView.ViewState.preview,
            sideText = SideTextView.ViewState.preview,
            type = "Market Order",
            size = "200",
            filledSize = "100",
            price = "$12.00",
            token = TokenTextView.ViewState.preview,
            logoUrl = "https://media.dydx.exchange/currencies/eth.png",
        )

        fun create(
            localizer: LocalizerProtocol,
            formatter: DydxFormatter,
            order: SubaccountOrder,
            marketMap: Map<String, PerpetualMarket>?,
            assetMap: Map<String, Asset>?,
        ): SharedOrderViewState? {
            val market = marketMap?.get(order.marketId) ?: return null
            val configs = market.configs ?: return null
            val asset = assetMap?.get(market.assetId) ?: return null

            val longValue = order.createdAtMilliseconds?.toLong()
            val remainingSize = order.remainingSize
            val filledSize = if (remainingSize != null && order.size != null) {
                order.size - remainingSize
            } else {
                null
            }
            val tickSize = configs.displayTickSizeDecimals ?: 1
            val stepSize = configs.displayStepSizeDecimals ?: 1

            return SharedOrderViewState(
                localizer = localizer,
                id = order.id,
                type = order.resources.typeString ?: localizer.localize(order.resources.typeStringKey ?: ""),
                status = order.resources.statusString ?: localizer.localize(order.resources.statusStringKey ?: ""),
                orderStatus = order.createOrderStatusViewState(localizer),
                sideText = SideTextView.ViewState(
                    localizer = localizer,
                    side = when (order.side) {
                        OrderSide.buy -> SideTextView.Side.Buy
                        OrderSide.sell -> SideTextView.Side.Sell
                    },
                ),
                date = if (longValue != null) {
                    IntervalText.ViewState(date = Instant.ofEpochMilli(longValue), formatter = formatter)
                } else {
                    null
                },
                size = formatter.localFormatted(order.size, stepSize),
                filledSize = formatter.localFormatted(filledSize, stepSize),
                price = when (order.type) {
                    OrderType.market, OrderType.stopMarket, OrderType.takeProfitMarket -> localizer.localize("APP.GENERAL.MARKET")
                    else -> formatter.dollar(order.price, tickSize)
                },
                triggerPrice = formatter.dollar(order.triggerPrice, tickSize),
                token = if (asset.id != null) {
                    TokenTextView.ViewState(
                        symbol = asset.id,
                    )
                } else {
                    null
                },
                logoUrl = asset.resources?.imageUrl,
            )
        }
    }
}

private fun SubaccountOrder.createOrderStatusViewState(localizer: LocalizerProtocol): OrderStatusView.ViewState {
    val color = when (this.status) {
        OrderStatus.cancelled, OrderStatus.canceling -> OrderStatusView.Status.Red
        OrderStatus.filled -> OrderStatusView.Status.Green
        OrderStatus.partiallyFilled, OrderStatus.pending -> OrderStatusView.Status.Yellow
        else -> OrderStatusView.Status.Blank
    }
    return OrderStatusView.ViewState(
        localizer = localizer,
        status = color,
    )
}
