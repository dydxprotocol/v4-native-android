package exchange.dydx.newsalerts

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.NewsAlertsRoutes
import exchange.dydx.trading.common.navigation.dydxComposable
import exchange.dydx.trading.feature.shared.bottombar.DydxBottomBarScaffold
import exchange.dydx.utilities.utils.Logging

fun NavGraphBuilder.newsAlertsGraph(
    appRouter: DydxRouter,
    logger: Logging,
) {
    dydxComposable(
        router = appRouter,
        route = NewsAlertsRoutes.main,
        deepLinks = appRouter.deeplinks(NewsAlertsRoutes.main),
    ) { navBackStackEntry ->
        DydxBottomBarScaffold(Modifier) {
            DydxNewsAlertsView.Content(Modifier)
        }
    }
}
