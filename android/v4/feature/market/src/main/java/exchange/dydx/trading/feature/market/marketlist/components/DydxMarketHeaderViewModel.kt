package exchange.dydx.trading.feature.market.marketlist.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.MarketRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxMarketHeaderViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxMarketHeaderView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxMarketHeaderView.ViewState {
        return DydxMarketHeaderView.ViewState(
            localizer = localizer,
            searchAction = {
                router.navigateTo(
                    route = MarketRoutes.marketSearch,
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
        )
    }
}
