package exchange.dydx.trading.feature.portfolio

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import exchange.dydx.abacus.protocols.LoggingProtocol
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.PortfolioRoutes
import exchange.dydx.trading.common.navigation.dydxComposable
import exchange.dydx.trading.feature.portfolio.components.fills.DydxPortfolioFillsView
import exchange.dydx.trading.feature.portfolio.components.orders.DydxPortfolioOrdersView
import exchange.dydx.trading.feature.portfolio.components.positions.DydxPortfolioPositionsView
import exchange.dydx.trading.feature.portfolio.components.transfers.DydxPortfolioTransfersView
import exchange.dydx.trading.feature.portfolio.orderdetails.DydxOrderDetailsView

private const val TAG = "PortfolioRouter"

fun NavGraphBuilder.portfolioGraph(
    appRouter: DydxRouter,
    logger: LoggingProtocol,
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
        deepLinks = appRouter.deeplinksWithParam(
            destination = PortfolioRoutes.order_details,
            param = "id",
            isPath = true,
        ),
    ) { navBackStackEntry ->
        val id = navBackStackEntry.arguments?.getString("id")
        if (id == null) {
            logger.e(TAG, "No identifier passed")
            appRouter.navigateTo(PortfolioRoutes.order_details)
            return@dydxComposable
        }
        DydxOrderDetailsView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = PortfolioRoutes.orders + "?showPortfolioSelector={showPortfolioSelector}",
        arguments = listOf(
            navArgument("showPortfolioSelector") {
                defaultValue = false
                type = NavType.BoolType
            },
        ),
        deepLinks = appRouter.deeplinksWithParam(
            destination = PortfolioRoutes.orders,
            param = "showPortfolioSelector",
            isPath = false,
        ),
    ) { navBackStackEntry ->
        val showPortfolioSelector = navBackStackEntry.arguments?.getBoolean("showPortfolioSelector") ?: false
        DydxPortfolioOrdersView.Content(Modifier, isFullScreen = true, showPortfolioSelector = showPortfolioSelector)
    }

    dydxComposable(
        router = appRouter,
        route = PortfolioRoutes.positions,
        deepLinks = appRouter.deeplinks(PortfolioRoutes.positions),
    ) { navBackStackEntry ->
        DydxPortfolioPositionsView.Content(Modifier, isFullScreen = true)
    }

    dydxComposable(
        router = appRouter,
        route = PortfolioRoutes.trades,
        deepLinks = appRouter.deeplinks(PortfolioRoutes.trades),
    ) { navBackStackEntry ->
        DydxPortfolioFillsView.Content(Modifier, isFullScreen = true)
    }

    dydxComposable(
        router = appRouter,
        route = PortfolioRoutes.transfers,
        deepLinks = appRouter.deeplinks(PortfolioRoutes.transfers),
    ) { navBackStackEntry ->
        DydxPortfolioTransfersView.Content(Modifier, isFullScreen = true)
    }
}
