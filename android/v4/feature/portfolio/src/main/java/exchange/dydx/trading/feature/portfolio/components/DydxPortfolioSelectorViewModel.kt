package exchange.dydx.trading.feature.portfolio.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.PortfolioRoutes
import exchange.dydx.trading.feature.portfolio.DydxPortfolioView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxPortfolioSelectorViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val displayContentFlow: MutableStateFlow<DydxPortfolioView.DisplayContent>,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {
    val state: Flow<DydxPortfolioSelectorView.ViewState?> = displayContentFlow
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(
        displayContent: DydxPortfolioView.DisplayContent
    ): DydxPortfolioSelectorView.ViewState? {
        return DydxPortfolioSelectorView.ViewState(
            localizer = localizer,
            currentContent = displayContent,
            onSelectionChanged = { displayContent ->
                when (displayContent) {
                    DydxPortfolioView.DisplayContent.Overview ->
                        router.navigateTo(PortfolioRoutes.main)

                    DydxPortfolioView.DisplayContent.Positions ->
                        router.navigateTo(PortfolioRoutes.positions)

                    DydxPortfolioView.DisplayContent.Orders ->
                        router.navigateTo(PortfolioRoutes.orders + "?showPortfolioSelector=true")

                    DydxPortfolioView.DisplayContent.Fees ->
                        router.navigateTo(PortfolioRoutes.fees)

                    DydxPortfolioView.DisplayContent.Transfers ->
                        router.navigateTo(PortfolioRoutes.transfers)

                    DydxPortfolioView.DisplayContent.Trades ->
                        router.navigateTo(PortfolioRoutes.trades)

                    DydxPortfolioView.DisplayContent.Funding ->
                        router.navigateTo(PortfolioRoutes.funding)
                }
                displayContentFlow.value = displayContent
            },
        )
    }
}
