package exchange.dydx.trading.feature.market.marketinfo.components.position

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.SubaccountPosition
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.BuildConfig
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.DydxFeatureFlag
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.TradeRoutes
import exchange.dydx.trading.feature.market.marketinfo.streams.MarketAndAsset
import exchange.dydx.trading.feature.market.marketinfo.streams.MarketInfoStreaming
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketPositionViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@HiltViewModel
class DydxMarketPositionViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val formatter: DydxFormatter,
    marketInfoStream: MarketInfoStreaming,
    private val router: DydxRouter,
    private val featureFlags: DydxFeatureFlags,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketPositionView.ViewState?> =
        combine(
            marketInfoStream.selectedSubaccountPosition.filterNotNull(),
            marketInfoStream.marketAndAsset.filterNotNull(),
        ) { selectedSubaccountPosition, marketAndAsset ->
            createViewState(selectedSubaccountPosition, marketAndAsset)
        }
            .distinctUntilChanged()

    private fun createViewState(
        position: SubaccountPosition,
        marketAndAsset: MarketAndAsset,
    ): DydxMarketPositionView.ViewState {
        return DydxMarketPositionView.ViewState(
            localizer = localizer,
            shareAction = {},
            closeAction = {
                router.navigateTo(
                    route = TradeRoutes.close_position + "/${marketAndAsset.market.id}",
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
            sharedMarketPositionViewState = SharedMarketPositionViewState.create(
                position = position,
                market = marketAndAsset.market,
                asset = marketAndAsset.asset,
                formatter = formatter,
                localizer = localizer,
                onAdjustMarginAction = {
                    router.navigateTo(
                        route = TradeRoutes.adjust_margin + "/${marketAndAsset.market.id}",
                        presentation = DydxRouter.Presentation.Modal,
                    )
                },
            ),
            enableTrigger = if (BuildConfig.DEBUG) {
                featureFlags.isFeatureEnabled(DydxFeatureFlag.enable_sl_tp_trigger)
            } else {
                featureFlags.isFeatureEnabled(DydxFeatureFlag.enable_sl_tp_trigger) &&
                    abacusStateManager.environment?.featureFlags?.isSlTpEnabled == true
            },
        )
    }
}
