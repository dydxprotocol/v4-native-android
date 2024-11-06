package exchange.dydx.trading.feature.market.marketinfo.components.stats

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.market.marketinfo.streams.MarketInfoStreaming
import exchange.dydx.trading.feature.shared.views.IntervalText
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class DydxMarketStatsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val formatter: DydxFormatter,
    marketInfoStream: MarketInfoStreaming,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketStatsView.ViewState?> =
        marketInfoStream.marketAndAsset
            .map { marketAndAsset ->
                if (marketAndAsset != null) {
                    createViewState(marketAndAsset.market, marketAndAsset.asset)
                } else {
                    null
                }
            }
            .distinctUntilChanged()

    private fun createViewState(
        market: PerpetualMarket,
        asset: Asset,
    ): DydxMarketStatsView.ViewState {
        var items: MutableList<DydxMarketStatsView.StatItem> = mutableListOf()

        val stepSize = market.configs?.displayStepSizeDecimals ?: 2
        val tickSize = market.configs?.displayTickSizeDecimals ?: 2

        val oraclePrice = formatter.dollar(number = market.oraclePrice, digits = tickSize) ?: "-"
        items.add(
            DydxMarketStatsView.StatItem(
                header = localizer.localize("APP.TRADE.ORACLE_PRICE"),
                value = SignedAmountView.ViewState(
                    text = oraclePrice,
                    sign = PlatformUISign.None,
                    coloringOption = SignedAmountView.ColoringOption.AllText,
                ),
            ),
        )

        val volume = formatter.dollarVolume(number = market.perpetual?.volume24H) ?: "-"
        items.add(
            DydxMarketStatsView.StatItem(
                header = localizer.localize("APP.TRADE.VOLUME_24H"),
                value = SignedAmountView.ViewState(
                    text = volume,
                    sign = PlatformUISign.None,
                    coloringOption = SignedAmountView.ColoringOption.AllText,
                ),
            ),
        )

        val priceChange24HPercent = market.priceChange24HPercent
        val change: String = if (priceChange24HPercent != null) {
            formatter.percent(number = priceChange24HPercent.absoluteValue, digits = 2) ?: "-"
        } else {
            "-"
        }
        items.add(
            DydxMarketStatsView.StatItem(
                header = localizer.localize("APP.TRADE.CHANGE_24H"),
                value = SignedAmountView.ViewState(
                    text = change,
                    sign = PlatformUISign.from(priceChange24HPercent),
                    coloringOption = SignedAmountView.ColoringOption.AllText,
                ),
            ),
        )

        val nextFundingRate = market.perpetual?.nextFundingRate
        val fundingRate: String = if (nextFundingRate != null) {
            formatter.percent(number = nextFundingRate.absoluteValue, digits = 6) ?: "-"
        } else {
            "-"
        }
        items.add(
            DydxMarketStatsView.StatItem(
                header = localizer.localize("APP.TRADE.FUNDING_RATE"),
                value = SignedAmountView.ViewState(
                    text = fundingRate,
                    sign = PlatformUISign.from(nextFundingRate),
                    coloringOption = SignedAmountView.ColoringOption.AllText,
                ),
            ),
        )

        val nextFundingAtMilliseconds = market.perpetual?.nextFundingAtMilliseconds
        val nextFundingViewState: IntervalText.ViewState
        if (nextFundingAtMilliseconds != null) {
            val nextFundingAt = Instant.ofEpochMilli(nextFundingAtMilliseconds.toLong())
            nextFundingViewState = IntervalText.ViewState(
                date = nextFundingAt,
                direction = IntervalText.Direction.COUNT_DOWN,
                format = IntervalText.Format.FULL,
            )
        } else {
            // With no nextFundingAt, we will just count down to the next hour mark
            nextFundingViewState = IntervalText.ViewState(
                direction = IntervalText.Direction.COUNT_DOWN_TO_HOUR,
                format = IntervalText.Format.FULL,
            )
        }
        items.add(
            DydxMarketStatsView.StatItem(
                header = localizer.localize("APP.TRADE.NEXT_FUNDING"),
                interval = nextFundingViewState,
            ),
        )

        val value = market.perpetual?.openInterest
        val openInterest: String = if (value != null) {
            formatter.condensed(number = value, digits = 2) ?: "-"
        } else {
            "-"
        }
        val token = TokenTextView.ViewState(symbol = asset.displayableAssetId)
        items.add(
            DydxMarketStatsView.StatItem(
                header = localizer.localize("APP.TRADE.OPEN_INTEREST"),
                value = SignedAmountView.ViewState(
                    text = openInterest,
                    sign = PlatformUISign.None,
                    coloringOption = SignedAmountView.ColoringOption.AllText,
                ),
                token = token,
            ),
        )

        return DydxMarketStatsView.ViewState(
            localizer = localizer,
            statItems = items,
        )
    }
}
