package exchange.dydx.trading.feature.portfolio

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.PortfolioRoutes
import exchange.dydx.trading.common.navigation.dydxComposable
import exchange.dydx.trading.feature.portfolio.orderdetails.DydxOrderDetailsView
import timber.log.Timber

fun NavGraphBuilder.portfolioGraph(
    appRouter: DydxRouter,
) {
    dydxComposable(
        router = appRouter,
        route = PortfolioRoutes.main,
        deepLinks = appRouter.deeplinks(PortfolioRoutes.main),
    ) { navBackStackEntry ->
        DydxPortfolioView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = PortfolioRoutes.order_details + "/{id}",
        arguments = listOf(navArgument("id") { type = NavType.StringType }),
        deepLinks = appRouter.deeplinksWithParam(PortfolioRoutes.order_details, "id", true),
    ) { navBackStackEntry ->
        val id = navBackStackEntry.arguments?.getString("id")
        if (id == null) {
            Timber.w("No identifier passed")
            appRouter.navigateTo(PortfolioRoutes.order_details)
            return@dydxComposable
        }
        DydxOrderDetailsView.Content(Modifier)
    }
}
