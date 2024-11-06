package exchange.dydx.trading.feature.shared.viewstate

import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.IntervalText
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import java.time.Instant

data class SharedFillViewState(
    val localizer: LocalizerProtocol,
    val id: String = "",
    val type: String? = null,
    val size: String? = null,
    val date: IntervalText.ViewState? = null,
    val price: String? = null,
    val fee: String? = null,
    val feeLiquidity: String? = null,
    val sideText: SideTextView.ViewState? = null,
    val token: TokenTextView.ViewState? = null,
    val logoUrl: String? = null,
) {
    companion object {
        val preview = SharedFillViewState(
            localizer = MockLocalizer(),
            type = "Market Order",
            size = "0.017 ETH",
            date = IntervalText.ViewState.preview,
            price = "$1,203.8",
            fee = "$0.0",
            feeLiquidity = "Taker",
            sideText = SideTextView.ViewState.preview,
            token = TokenTextView.ViewState.preview,
            logoUrl = "https://media.dydx.exchange/currencies/eth.png",
        )

        fun create(
            localizer: LocalizerProtocol,
            formatter: DydxFormatter,
            fill: SubaccountFill,
            marketMap: Map<String, PerpetualMarket>?,
            assetMap: Map<String, Asset>?,
        ): SharedFillViewState? {
            val market = marketMap?.get(fill.marketId) ?: return null
            val configs = market.configs ?: return null
            val asset = assetMap?.get(market.assetId) ?: return null

            val longValue = fill.createdAtMilliseconds?.toLong()

            val tickSize = configs.displayTickSizeDecimals ?: 1
            val stepSize = configs.displayStepSizeDecimals ?: 1

            return SharedFillViewState(
                localizer = localizer,
                id = fill.id,
                type = fill.resources.typeString ?: localizer.localize(fill.resources.typeStringKey ?: ""),
                size = formatter.localFormatted(fill.size, stepSize),
                date = if (longValue != null) {
                    IntervalText.ViewState(
                        date = Instant.ofEpochMilli(longValue),
                    )
                } else {
                    null
                },
                price = formatter.dollar(fill.price, tickSize),
                fee = formatter.dollar(fill.fee, tickSize),
                feeLiquidity = fill.resources.liquidityString ?: localizer.localize(fill.resources.liquidityStringKey ?: ""),
                sideText = SideTextView.ViewState(
                    localizer = localizer,
                    side = when (fill.side) {
                        OrderSide.Buy -> SideTextView.Side.Buy
                        OrderSide.Sell -> SideTextView.Side.Sell
                    },
                ),
                token = if (asset.displayableAssetId.isNotEmpty()) {
                    TokenTextView.ViewState(
                        symbol = asset.displayableAssetId,
                    )
                } else {
                    null
                },
                logoUrl = asset.resources?.imageUrl,
            )
        }
    }
}
