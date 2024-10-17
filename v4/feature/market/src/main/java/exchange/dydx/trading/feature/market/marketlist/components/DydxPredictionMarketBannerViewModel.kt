package exchange.dydx.trading.feature.market.marketlist.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.MarketRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DydxPredictionMarketBannerViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxPredictionMarketBannerView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxPredictionMarketBannerView.ViewState? {
        // logic here turns this banner display off after election day
        // Nov 6 12am ET https://currentmillis.com/?1730869200010
        val electionDate = Instant.ofEpochMilli(1730869200010L)
        val now = Instant.now()
        return if (now.isBefore(electionDate)) {
            DydxPredictionMarketBannerView.ViewState(
                localizer = localizer,
                onTapAction = {
                    router.navigateTo(
                        route = MarketRoutes.marketInfo + "/TRUMPWIN-USD",
                        presentation = DydxRouter.Presentation.Push,
                    )
                },
            )
        } else {
            null
        }
    }
}
