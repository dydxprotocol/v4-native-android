package exchange.dydx.trading.feature.market.marketlist.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.clientState.favorite.DydxFavoriteStoreProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxMarketAssetFilterViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
    private val mutableFilterActionFlow: MutableStateFlow<FilterAction>,
) : ViewModel(), DydxViewModel {

    private val actions: List<FilterAction>
        get() = FilterAction.actions(localizer)

    val state: Flow<DydxMarketAssetFilterView.ViewState?> = mutableFilterActionFlow
        .map { filterAction ->
            createViewState(actions.indexOf(filterAction))
        }
        .distinctUntilChanged()

    private fun createViewState(selectedIndex: Int): DydxMarketAssetFilterView.ViewState {
        return DydxMarketAssetFilterView.ViewState(
            localizer = localizer,
            contents = actions.map { it.content },
            onSelectionChanged = {
                mutableFilterActionFlow.value = actions[it]
            },
            selectedIndex = selectedIndex,
        )
    }
}

enum class MarketFiltering {
    ALL,
    FAVORITED,
    LAYER1,
    LAYER2,
    DEFI,
}

data class FilterAction(
    val type: MarketFiltering,
    val content: String,
    val action: (PerpetualMarket, Map<String, Asset>, DydxFavoriteStoreProtocol) -> Boolean,
) {
    companion object {
        fun actions(localizer: LocalizerProtocol): List<FilterAction> {
            return listOf(
                FilterAction(
                    type = MarketFiltering.ALL,
                    content = localizer.localize("APP.GENERAL.ALL"),
                    action = { _, _, _, -> true }, // included
                ),

                FilterAction(
                    type = MarketFiltering.FAVORITED,
                    content = localizer.localize("APP.GENERAL.SAVED"),
                    action = { market, _, favStore ->
                        favStore.isFavorite(market.id)
                    },
                ),

                FilterAction(
                    type = MarketFiltering.LAYER1,
                    content = localizer.localize("APP.GENERAL.LAYER_1"),
                    action = { market, assetMap, _ ->
                        assetMap[market.assetId]?.tags?.contains("Layer 1") ?: false
                    },
                ),

                FilterAction(
                    type = MarketFiltering.DEFI,
                    content = localizer.localize("APP.GENERAL.DEFI"),
                    action = { market, assetMap, _ ->
                        assetMap[market.assetId]?.tags?.contains("Defi") ?: false
                    },
                ),
            )
        }
    }
}
