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
    PREDICTION,
    LAYER1,
    LAYER2,
    DEFI,
    DEPIN,
    NEW,
    AI,
    NFT,
    GAMING,
    MEME,
    RWA,
    FOREX,
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
                    type = MarketFiltering.NEW,
                    content = localizer.localize("APP.GENERAL.RECENTLY_LISTED"),
                    action = { market, _, _ ->
                        market.perpetual?.isNew ?: false
                    },
                ),

                /*
                FilterAction(
                    type = MarketFiltering.PREDICTION,
                    content = localizer.localize("APP.GENERAL.PREDICTION_MARKET"),
                    action = { market, assetMap, _ ->
                        assetMap[market.assetId]?.tags?.contains("Prediction Market") ?: false
                    },
                ),
                 */

                FilterAction(
                    type = MarketFiltering.MEME,
                    content = localizer.localize("APP.GENERAL.MEME"),
                    action = { market, assetMap, _ ->
                        assetMap[market.assetId]?.tags?.contains("memes") ?: false
                    },
                ),

                FilterAction(
                    type = MarketFiltering.AI,
                    content = localizer.localize("APP.GENERAL.AI"),
                    action = { market, assetMap, _ ->
                        assetMap[market.assetId]?.tags?.contains("ai-big-data") ?: false
                    },
                ),

                FilterAction(
                    type = MarketFiltering.DEFI,
                    content = localizer.localize("APP.GENERAL.DEFI"),
                    action = { market, assetMap, _ ->
                        assetMap[market.assetId]?.tags?.contains("defi") ?: false
                    },
                ),

                FilterAction(
                    type = MarketFiltering.DEPIN,
                    content = localizer.localize("APP.GENERAL.DEPIN"),
                    action = { market, assetMap, _ ->
                        assetMap[market.assetId]?.tags?.contains("depin") ?: false
                    },
                ),

                FilterAction(
                    type = MarketFiltering.LAYER1,
                    content = localizer.localize("APP.GENERAL.LAYER_1"),
                    action = { market, assetMap, _ ->
                        assetMap[market.assetId]?.tags?.contains("layer-1") ?: false
                    },
                ),

                FilterAction(
                    type = MarketFiltering.LAYER2,
                    content = localizer.localize("APP.GENERAL.LAYER_2"),
                    action = { market, assetMap, _ ->
                        assetMap[market.assetId]?.tags?.contains("layer-2") ?: false
                    },
                ),

                FilterAction(
                    type = MarketFiltering.RWA,
                    content = localizer.localize("APP.GENERAL.REAL_WORLD_ASSET_SHORT"),
                    action = { market, assetMap, _ ->
                        assetMap[market.assetId]?.tags?.contains("real-world-assets") ?: false
                    },
                ),

                FilterAction(
                    type = MarketFiltering.GAMING,
                    content = localizer.localize("APP.GENERAL.GAMING"),
                    action = { market, assetMap, _ ->
                        assetMap[market.assetId]?.tags?.contains("gaming") ?: false
                    },
                ),

                FilterAction(
                    type = MarketFiltering.FOREX,
                    content = localizer.localize("APP.GENERAL.FOREX"),
                    action = { market, assetMap, _ ->
                        assetMap[market.assetId]?.tags?.contains("fiat") ?: false
                    },
                ),
            )
        }
    }
}
