package exchange.dydx.trading.feature.portfolio

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.PortfolioRoutes
import exchange.dydx.trading.common.navigation.dydxComposable
import exchange.dydx.trading.feature.portfolio.cancelpendingposition.DydxCancelPendingPositionView
import exchange.dydx.trading.feature.portfolio.components.fills.DydxPortfolioFillsView
import exchange.dydx.trading.feature.portfolio.components.fundings.DydxPortfolioFundingsView
import exchange.dydx.trading.feature.portfolio.components.orders.DydxPortfolioOrdersView
import exchange.dydx.trading.feature.portfolio.components.positions.DydxPortfolioPositionsView
import exchange.dydx.trading.feature.portfolio.components.transfers.DydxPortfolioTransfersView
import exchange.dydx.trading.feature.portfolio.fundingdetails.DydxFundingDetailsView
import exchange.dydx.trading.feature.portfolio.orderdetails.DydxOrderDetailsView
import exchange.dydx.utilities.utils.Logging

private const val TAG = "PortfolioRouter"

fun NavGraphBuilder.portfolioGraph(
    appRouter: DydxRouter,
    logger: Logging,
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
        deepLinks = appRouter.deeplinks(
            destination = PortfolioRoutes.order_details,
            path = "id",
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
        route = PortfolioRoutes.funding_details + "/{id}",
        arguments = listOf(navArgument("id") { type = NavType.StringType }),
        deepLinks = appRouter.deeplinks(
            destination = PortfolioRoutes.funding_details,
            path = "id",
        ),
    ) { navBackStackEntry ->
        val id = navBackStackEntry.arguments?.getString("id")
        if (id == null) {
            logger.e(TAG, "No identifier passed")
            appRouter.navigateTo(PortfolioRoutes.funding_details)
            return@dydxComposable
        }
        DydxFundingDetailsView.Content(Modifier)
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
        deepLinks = appRouter.deeplinks(
            destination = PortfolioRoutes.orders,
            params = listOf("showPortfolioSelector"),
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
        route = PortfolioRoutes.funding,
        deepLinks = appRouter.deeplinks(PortfolioRoutes.trades),
    ) { navBackStackEntry ->
        DydxPortfolioFundingsView.Content(Modifier, isFullScreen = true)
    }

    dydxComposable(
        router = appRouter,
        route = PortfolioRoutes.transfers,
        deepLinks = appRouter.deeplinks(PortfolioRoutes.transfers),
    ) { navBackStackEntry ->
        DydxPortfolioTransfersView.Content(Modifier, isFullScreen = true)
    }

    dydxComposable(
        router = appRouter,
        route = PortfolioRoutes.cancel_pending_position + "/{marketId}",
        arguments = listOf(navArgument("marketId") { type = NavType.StringType }),
        deepLinks = appRouter.deeplinks(
            destination = PortfolioRoutes.cancel_pending_position,
            path = "marketId",
        ),
    ) { navBackStackEntry ->
        val id = navBackStackEntry.arguments?.getString("marketId")
        if (id == null) {
            logger.e(TAG, "No identifier passed")
            appRouter.navigateTo(PortfolioRoutes.cancel_pending_position)
            return@dydxComposable
        }
        DydxCancelPendingPositionView.Content(Modifier)
    }
}
