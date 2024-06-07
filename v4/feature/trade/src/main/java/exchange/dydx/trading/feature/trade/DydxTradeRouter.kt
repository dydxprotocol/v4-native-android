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
import exchange.dydx.trading.feature.trade.margin.DydxAdjustMarginInputView
import exchange.dydx.trading.feature.trade.marginmode.DydxTradeInputMarginModeView
import exchange.dydx.trading.feature.trade.targetleverage.DydxTradeInputTargetLeverageView
import exchange.dydx.trading.feature.trade.tradestatus.DydxTradeStatusView
import exchange.dydx.trading.feature.trade.trigger.DydxTriggerOrderInputView
import exchange.dydx.utilities.utils.Logging

private const val TAG = "DydxTradeRouter"

fun NavGraphBuilder.tradeGraph(
    appRouter: DydxRouter,
    logger: Logging,
) {
    dydxComposable(
        router = appRouter,
        route = TradeRoutes.status + "/{tradeType}",
        arguments = listOf(navArgument("tradeType") { type = NavType.StringType }),
        deepLinks = appRouter.deeplinksWithParam(TradeRoutes.status, "tradeType", true),
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
            logger.e(TAG, "No marketId passed")
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
            logger.e(TAG, "No marketId passed")
            appRouter.navigateTo(MarketRoutes.marketList)
            return@dydxComposable
        }
        DydxTriggerOrderInputView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = TradeRoutes.margin_mode,
        deepLinks = appRouter.deeplinks(TradeRoutes.margin_mode),
    ) { navBackStackEntry ->
        DydxTradeInputMarginModeView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = TradeRoutes.target_leverage,
        deepLinks = appRouter.deeplinks(TradeRoutes.target_leverage),
    ) { navBackStackEntry ->
        DydxTradeInputTargetLeverageView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = TradeRoutes.adjust_margin + "/{marketId}",
        arguments = listOf(navArgument("marketId") { type = NavType.StringType }),
        deepLinks = appRouter.deeplinksWithParam(TradeRoutes.adjust_margin, "marketId", true),
    ) { navBackStackEntry ->
        val id = navBackStackEntry.arguments?.getString("marketId")
        if (id == null) {
            logger.e(TAG, "No marketId passed")
            appRouter.navigateTo(MarketRoutes.marketList)
            return@dydxComposable
        }
        DydxAdjustMarginInputView.Content(Modifier)
    }
}
