package exchange.dydx.trading.feature.transfer

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.TransferRoutes
import exchange.dydx.trading.common.navigation.dydxComposable
import exchange.dydx.trading.feature.transfer.search.DydxTransferSearchView
import exchange.dydx.trading.feature.transfer.status.DydxTransferStatusView
import exchange.dydx.utilities.utils.Logging

private const val TAG = "DydxTransferRouter"

fun NavGraphBuilder.transferGraph(
    appRouter: DydxRouter,
    logger: Logging,
) {
    dydxComposable(
        router = appRouter,
        route = TransferRoutes.transfer,
        deepLinks = appRouter.deeplinks(TransferRoutes.transfer),
    ) { navBackStackEntry ->
        DydxTransferView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = TransferRoutes.transfer_search,
        deepLinks = appRouter.deeplinks(TransferRoutes.transfer_search),
    ) { navBackStackEntry ->
        DydxTransferSearchView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = TransferRoutes.transfer_status + "/{hash}",
        arguments = listOf(navArgument("hash") { type = NavType.StringType }),
        deepLinks = appRouter.deeplinks(
            destination = TransferRoutes.transfer_status,
            path = "hash",
        ),
    ) { navBackStackEntry ->
        val hash = navBackStackEntry.arguments?.getString("hash")
        if (hash == null) {
            logger.e(TAG, "No hash passed")
            appRouter.navigateTo(TransferRoutes.transfer)
            return@dydxComposable
        }
        DydxTransferStatusView.Content(Modifier)
    }
}
