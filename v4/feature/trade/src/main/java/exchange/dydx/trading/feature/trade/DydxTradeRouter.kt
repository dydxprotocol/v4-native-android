package exchange.dydx.trading.feature.trade

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.MarketRoutes
import exchange.dydx.trading.common.navigation.TradeRoutes
import exchange.dydx.trading.common.navigation.dydxComposable
import exchange.dydx.trading.feature.trade.closeposition.DydxClosePositionInputView
import exchange.dydx.trading.feature.trade.tradestatus.DydxTradeStatusView
import exchange.dydx.trading.feature.trade.trigger.DydxTakeProfitStopLossView
import timber.log.Timber

private const val TAG = "DydxTradeRouter"

fun NavGraphBuilder.tradeGraph(
    appRouter: DydxRouter,
) {
    dydxComposable(
        router = appRouter,
        route = TradeRoutes.status,
        deepLinks = appRouter.deeplinks(TradeRoutes.status),
    ) { navBackStackEntry ->
        DydxTradeStatusView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = TradeRoutes.close_position + "/{marketId}",
        arguments = listOf(navArgument("marketId") { type = NavType.StringType }),
        deepLinks = appRouter.deeplinksWithParam(TradeRoutes.close_position, "marketId", true),
    ) { navBackStackEntry ->
        val id = navBackStackEntry.arguments?.getString("marketId")
        if (id == null) {
            Timber.w("No marketId passed")
            appRouter.navigateTo(MarketRoutes.marketList)
            return@dydxComposable
        }
        DydxClosePositionInputView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = TradeRoutes.trigger + "/{marketId}",
        arguments = listOf(navArgument("marketId") { type = NavType.StringType }),
        deepLinks = appRouter.deeplinksWithParam(TradeRoutes.trigger, "marketId", true),
    ) { navBackStackEntry ->
        val id = navBackStackEntry.arguments?.getString("marketId")
        if (id == null) {
            Timber.w("No marketId passed")
            appRouter.navigateTo(MarketRoutes.marketList)
            return@dydxComposable
        }
        DydxTakeProfitStopLossView.Content(Modifier)
    }
}
