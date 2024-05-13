package exchange.dydx.trading.feature.market

import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import exchange.dydx.abacus.protocols.LoggingProtocol
import exchange.dydx.platformui.components.PlatformBottomSheet
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.MarketRoutes
import exchange.dydx.trading.common.navigation.dydxComposable
import exchange.dydx.trading.feature.market.marketinfo.DydxMarketInfoView
import exchange.dydx.trading.feature.market.marketlist.DydxMarketAssetListView
import exchange.dydx.trading.feature.market.search.DydxMarketSearchView
import exchange.dydx.trading.feature.trade.tradeinput.DydxTradeInputView

private const val TAG = "MarketRouter"

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.marketGraph(
    appRouter: DydxRouter,
    logger: LoggingProtocol,
) {
    dydxComposable(
        router = appRouter,
        route = MarketRoutes.marketList,
        deepLinks = appRouter.deeplinks(MarketRoutes.marketList),
    ) { navBackStackEntry ->
        DydxMarketAssetListView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = MarketRoutes.marketInfo + "/{marketId}",
        arguments = listOf(navArgument("marketId") { type = NavType.StringType }),
        deepLinks = appRouter.deeplinksWithParam(MarketRoutes.marketInfo, "marketId", true),
    ) { navBackStackEntry ->
        val id = navBackStackEntry.arguments?.getString("marketId")
        if (id == null) {
            logger.e(TAG, "No identifier passed")
            appRouter.navigateTo(MarketRoutes.marketList)
            return@dydxComposable
        }

        val scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(
//                bottomSheetState = SheetState(
//                    skipPartiallyExpanded = false,
//                    initialValue = SheetValue.Hidden,
//                ),
        )

        PlatformBottomSheet(
            Modifier,
            sheetPeekHeight = DydxTradeInputView.sheetPeekHeight,
            scaffoldState = scaffoldState,
            sheetContent = {
                DydxTradeInputView.bottomSheetState = scaffoldState.bottomSheetState
                DydxTradeInputView.Content(Modifier)
            },
        ) {
            DydxMarketInfoView.Content(Modifier)
        }
    }

    dydxComposable(
        router = appRouter,
        route = MarketRoutes.marketSearch,
        deepLinks = appRouter.deeplinks(MarketRoutes.marketSearch),
    ) { navBackStackEntry ->
        DydxMarketSearchView.Content(Modifier)
    }
}
