package exchange.dydx.trading.feature.market.marketlist.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxMarketAssetSortViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
    private val mutableSortActionFlow: MutableStateFlow<SortAction>,
) : ViewModel(), DydxViewModel {

    private val actions: List<SortAction>
        get() = SortAction.actions(localizer)

    val state: Flow<DydxMarketAssetSortView.ViewState?> =
        mutableSortActionFlow
            .map { sortAction ->
                createViewState(actions.indexOf(sortAction))
            }
            .distinctUntilChanged()

    private fun createViewState(selectedIndex: Int): DydxMarketAssetSortView.ViewState {
        return DydxMarketAssetSortView.ViewState(
            localizer = localizer,
            contents = actions.map { it.text },
            onSelectionChanged = { index ->
                mutableSortActionFlow.value = actions[index]
            },
            selectedIndex = selectedIndex,
        )
    }
}

data class SortAction(
    val type: MarketSorting,
    val text: String,
    val action: (PerpetualMarket, PerpetualMarket) -> Int,
) {
    companion object {
        fun actions(localizer: LocalizerProtocol): List<SortAction> {
            return listOf(
                SortAction(
                    type = MarketSorting.Volume24H,
                    text = localizer.localize("APP.TRADE.VOLUME"),
                    action = { first, second ->
                        if (first.perpetual?.volume24H?.toDouble() ?: 0.0 < second.perpetual?.volume24H?.toDouble() ?: 0.0) {
                            return@SortAction 1
                        } else {
                            return@SortAction -1
                        }
                    },
                ),
                SortAction(
                    type = MarketSorting.Gainers,
                    text = localizer.localize("APP.GENERAL.GAINERS"),
                    action = { first, second ->
                        if (first.priceChange24HPercent?.toDouble() ?: 0.0 < second.priceChange24HPercent?.toDouble() ?: 0.0) {
                            return@SortAction 1
                        } else {
                            return@SortAction -1
                        }
                    },
                ),
                SortAction(
                    type = MarketSorting.Losers,
                    text = localizer.localize("APP.GENERAL.LOSERS"),
                    action = { first, second ->
                        if (first.priceChange24HPercent?.toDouble() ?: 0.0 > second.priceChange24HPercent?.toDouble() ?: 0.0) {
                            return@SortAction 1
                        } else {
                            return@SortAction -1
                        }
                    },
                ),
                SortAction(
                    type = MarketSorting.FundingRate,
                    text = localizer.localize("APP.GENERAL.FUNDING_RATE_CHART_SHORT"),
                    action = { first, second ->
                        if (first.perpetual?.nextFundingRate?.toDouble() ?: 0.0 < second.perpetual?.nextFundingRate?.toDouble() ?: 0.0) {
                            return@SortAction 1
                        } else {
                            return@SortAction -1
                        }
                    },
                ),
                SortAction(
                    type = MarketSorting.Name,
                    text = localizer.localize("APP.GENERAL.NAME"),
                    action = { first, second ->
                        (first.market ?: "").compareTo(second.market ?: "", ignoreCase = true)
                    },
                ),
                SortAction(
                    type = MarketSorting.Price,
                    text = localizer.localize("APP.GENERAL.PRICE"),
                    action = { first, second ->
                        if (first.oraclePrice?.toDouble() ?: 0.0 < second.oraclePrice?.toDouble() ?: 0.0) {
                            return@SortAction 1
                        } else {
                            return@SortAction -1
                        }
                    },
                ),
            )
        }
    }
}

enum class MarketSorting {
    Volume24H, Gainers, Losers, FundingRate, Name, Price
}
