package exchange.dydx.trading.feature.market.marketinfo.components.configs

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.market.marketinfo.streams.MarketInfoStreaming
import exchange.dydx.trading.feature.shared.views.TokenTextView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxMarketConfigsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val formatter: DydxFormatter,
    marketInfoStream: MarketInfoStreaming,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketConfigsView.ViewState?> =
        marketInfoStream.market
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(market: PerpetualMarket?): DydxMarketConfigsView.ViewState {
        val marketConfigs = market?.configs

        val maxLeverageText: String?
        val initialMarginFraction = marketConfigs?.initialMarginFraction
        if (initialMarginFraction != null && initialMarginFraction > 0) {
            maxLeverageText = formatter.localFormatted(1.0 / initialMarginFraction)
        } else {
            maxLeverageText = null
        }

        val items: List<DydxMarketConfigsView.Item> = listOf(
            DydxMarketConfigsView.Item(
                title = localizer.localize("APP.GENERAL.MARKET_NAME"),
                value = market?.displayId ?: "-",
            ),
            DydxMarketConfigsView.Item(
                title = localizer.localize("APP.GENERAL.TICK_SIZE"),
                value = "$" + formatter.localFormatted(marketConfigs?.tickSize),
            ),
            DydxMarketConfigsView.Item(
                title = localizer.localize("APP.GENERAL.STEP_SIZE"),
                value = formatter.localFormatted(marketConfigs?.stepSize) ?: "-",
                tokenText = market?.let { TokenTextView.ViewState(symbol = it.assetId) },
            ),
            DydxMarketConfigsView.Item(
                title = localizer.localize("APP.GENERAL.MINIMUM_ORDER_SIZE"),
                value = formatter.localFormatted(marketConfigs?.minOrderSize) ?: "-",
                tokenText = market?.let { TokenTextView.ViewState(symbol = it.assetId) },
            ),
            DydxMarketConfigsView.Item(
                title = localizer.localize("APP.GENERAL.MAXIMUM_LEVERAGE"),
                value = maxLeverageText + "x",
            ),
            DydxMarketConfigsView.Item(
                title = localizer.localize("APP.GENERAL.MAINTENANCE_MARGIN_FRACTION"),
                value = formatter.percent(marketConfigs?.maintenanceMarginFraction, 4) ?: "-",
            ),
            DydxMarketConfigsView.Item(
                title = localizer.localize("APP.GENERAL.INITIAL_MARGIN_FRACTION"),
                value = formatter.percent(marketConfigs?.initialMarginFraction, 4) ?: "-",
            ),
            DydxMarketConfigsView.Item(
                title = localizer.localize("APP.GENERAL.BASE_POSITION_NOTIONAL"),
                value = formatter.localFormatted(marketConfigs?.basePositionNotional) ?: "-",
            ),
        )
        return DydxMarketConfigsView.ViewState(
            localizer = localizer,
            items = items,
        )
    }
}
