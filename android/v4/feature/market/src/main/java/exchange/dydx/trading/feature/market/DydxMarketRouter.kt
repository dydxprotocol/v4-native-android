package exchange.dydx.trading.feature.market

import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import exchange.dydx.platformui.components.PlatformBottomSheet
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.MarketRoutes
import exchange.dydx.trading.common.navigation.dydxComposable
import exchange.dydx.trading.feature.market.marketinfo.DydxMarketInfoView
import exchange.dydx.trading.feature.market.marketinfo.components.tabs.DydxMarketAccountTabView
import exchange.dydx.trading.feature.market.marketlist.DydxMarketAssetListView
import exchange.dydx.trading.feature.market.search.DydxMarketSearchView
import exchange.dydx.trading.feature.trade.tradeinput.DydxTradeInputView
import exchange.dydx.utilities.utils.Logging

private const val TAG = "MarketRouter"

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.marketGraph(
    appRouter: DydxRouter,
    logger: Logging,
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
        route = MarketRoutes.marketInfo + "/{marketId}?currentSection={currentSection}",
        arguments = listOf(
            navArgument("marketId") {
                type = NavType.StringType
            },
            navArgument("currentSection") {
                type = NavType.StringType
                nullable = true
                defaultValue = DydxMarketAccountTabView.Selection.Position.name
            },
        ),
        deepLinks = appRouter.deeplinks(
            destination = MarketRoutes.marketInfo,
            path = "marketId",
            params = listOf("currentSection"),
        ),
    ) { navBackStackEntry ->
        val id = navBackStackEntry.arguments?.getString("marketId")
        if (id == null) {
            logger.e(TAG, "No identifier passed")
            appRouter.navigateTo(MarketRoutes.marketList)
            return@dydxComposable
        }

        val scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = SheetState(
                skipPartiallyExpanded = false,
                density = LocalDensity.current,
                initialValue = SheetValue.PartiallyExpanded,
                skipHiddenState = true,
            ),
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
