package exchange.dydx.trading.feature.shared.viewstate

import com.github.mikephil.charting.data.Entry
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.clientState.favorite.DydxFavoriteStoreProtocol
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.platformui.components.charts.view.LineChartDataSet
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.SignedAmountView

data class SharedMarketViewState(
    val id: String? = null,
    val tokenSymbol: String? = null,
    val tokenFullName: String? = null,
    val logoUrl: String? = null,
    val volume24H: String? = null,
    val indexPrice: String? = null,
    val priceChangePercent24H: SignedAmountView.ViewState? = null,
    val primaryDescription: String? = null,
    val secondaryDescription: String? = null,
    val whitepaperUrl: String? = null,
    val websiteUrl: String? = null,
    val coinMarketPlaceUrl: String? = null,
    val sparkline: LineChartDataSet? = null,
    var isFavorite: Boolean = false,
) {
    companion object {
        val preview: SharedMarketViewState by lazy {
            SharedMarketViewState(
                id = "ETH-USD",
                tokenSymbol = "ETH",
                tokenFullName = "Ethereum",
                logoUrl = "https://media.dydx.exchange/currencies/eth.png",
                volume24H = "$223M",
                indexPrice = "$1000.00",
                priceChangePercent24H = SignedAmountView.ViewState(
                    text = "0.2%",
                    sign = PlatformUISign.Plus,
                    coloringOption = SignedAmountView.ColoringOption.AllText,
                ),
                primaryDescription = "Ethereum is a global, open-source platform for decentralized applications.",
                secondaryDescription = "Ethereum is a decentralized blockchain platform founded in 2014. Ethereum is an open-source project that is not owned or operated by a single individual. This means that anyone, anywhere can download the software and begin interacting with the network. Ethereum allows developers to make and operate 'smart contracts', a core piece of infrastructure for any decentralized application.",
                websiteUrl = "https://www.getmonero.org/",
                whitepaperUrl = "https://www.getmonero.org/resources/research-lab/",
                coinMarketPlaceUrl = "https://coinmarketcap.com/currencies/monero/",
                isFavorite = true,
            )
        }

        fun create(
            market: PerpetualMarket,
            asset: Asset?,
            formatter: DydxFormatter,
            localizer: LocalizerProtocol,
            favoriteStore: DydxFavoriteStoreProtocol,
        ): SharedMarketViewState {
            val viewModel = SharedMarketViewState(
                id = market.displayId,
                tokenSymbol = asset?.displayableAssetId ?: market.assetId,
                tokenFullName = asset?.name ?: market.assetId,
                logoUrl = asset?.resources?.imageUrl,
                volume24H = formatter.dollarVolume(market.perpetual?.volume24H),
                indexPrice = formatter.dollar(market.oraclePrice, market.configs?.displayTickSizeDecimals ?: 2),
                priceChangePercent24H = SignedAmountView.ViewState(
                    text = formatter.percent(
                        Math.abs(market.priceChange24HPercent?.toDouble() ?: 0.0),
                        2,
                    ),
                    sign = if (market.priceChange24HPercent?.toDouble() ?: 0.0 >= 0) PlatformUISign.Plus else PlatformUISign.Minus,
                    coloringOption = SignedAmountView.ColoringOption.AllText,
                ),
                primaryDescription = asset?.resources?.primaryDescriptionKey?.let { key ->
                    localizer.localize("APP.$key").takeUnless { it.startsWith("APP.__ASSETS.") }
                },
                secondaryDescription = asset?.resources?.secondaryDescriptionKey?.let { key ->
                    localizer.localize("APP.$key").takeUnless { it.startsWith("APP.__ASSETS.") }
                },
                websiteUrl = asset?.resources?.websiteLink,
                whitepaperUrl = asset?.resources?.whitepaperLink,
                coinMarketPlaceUrl = asset?.resources?.coinMarketCapsLink,
                sparkline = market.perpetual?.line?.let { line ->
                    val entries = mutableListOf<Entry>()
                    for (i in line.indices) {
                        entries.add(
                            Entry(i.toFloat(), line[i].toFloat()),
                        )
                    }
                    LineChartDataSet(
                        entries,
                        "Sparkline",
                    )
                },
                isFavorite = favoriteStore.isFavorite(market.id),
            )
            return viewModel
        }
    }
}
