package exchange.dydx.trading.feature.shared.bottombar

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.DydxFeatureFlag
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.MarketRoutes
import exchange.dydx.trading.common.navigation.NewsAlertsRoutes
import exchange.dydx.trading.common.navigation.PortfolioRoutes
import exchange.dydx.trading.common.navigation.ProfileRoutes
import exchange.dydx.trading.common.navigation.VaultRoutes
import exchange.dydx.trading.feature.shared.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class DydxBottomBarModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val featureFlags: DydxFeatureFlags,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxBottomBar.ViewState?> = MutableStateFlow(createViewState())

    private fun createViewState(): DydxBottomBar.ViewState {
        val barItems: List<BottomBarItem> = listOf(
            portfolioItem(router),
            marketItem(router),
            centerButton(router),
            if (featureFlags.isFeatureEnabled(DydxFeatureFlag.vault_enabled, default = true)) {
                vaultItem(router)
            } else {
                newsAlertsItem(router)
            },
            profileItem(router),
        )
        return DydxBottomBar.ViewState(
            localizer = localizer,
            items = barItems,
        )
    }

    private fun centerButton(router: DydxRouter) = BottomBarItem(
        route = null,
        label = null,
        icon = null,
        centerButton = true,
        onTapAction = {
            val market = "ETH-USD"
            abacusStateManager.setMarket(market)
            router.navigateTo(MarketRoutes.marketInfo + "/$market")
        },
    )

    private fun portfolioItem(router: DydxRouter) = BottomBarItem(
        route = PortfolioRoutes.main,
        label = "APP.PORTFOLIO.PORTFOLIO",
        icon = R.drawable.ic_tap_portfolio,
        selected = router.routeIsInBackStack(PortfolioRoutes.main),
        onTapAction = {
            router.tabTo(PortfolioRoutes.main)
        },
    )

    private fun marketItem(router: DydxRouter) = BottomBarItem(
        route = MarketRoutes.marketList,
        label = "APP.GENERAL.MARKETS",
        icon = R.drawable.ic_tab_markets,
        selected = router.routeIsInBackStack(MarketRoutes.marketList),
        onTapAction = {
            router.tabTo(MarketRoutes.marketList)
        },
    )

    private fun newsAlertsItem(router: DydxRouter) = BottomBarItem(
        route = NewsAlertsRoutes.main,
        label = "APP.GENERAL.ALERTS",
        icon = R.drawable.ic_tap_alerts,
        selected = router.routeIsInBackStack(NewsAlertsRoutes.main),
        onTapAction = {
            router.tabTo(NewsAlertsRoutes.main)
        },
    )

    private fun profileItem(router: DydxRouter) = BottomBarItem(
        route = ProfileRoutes.main,
        label = "APP.GENERAL.PROFILE",
        icon = R.drawable.ic_tab_profile,
        selected = router.routeIsInBackStack(ProfileRoutes.main),
        onTapAction = {
            router.tabTo(ProfileRoutes.main)
        },
    )

    private fun vaultItem(router: DydxRouter) = BottomBarItem(
        route = NewsAlertsRoutes.main,
        label = "APP.VAULTS.VAULT",
        icon = R.drawable.ic_tab_vault,
        selected = router.routeIsInBackStack(VaultRoutes.main),
        onTapAction = {
            router.tabTo(VaultRoutes.main)
        },
    )
}
