package exchange.dydx.trading.feature.transfer

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.TransferRoutes
import exchange.dydx.trading.common.navigation.TransferRoutes.transfer_deposit_noble
import exchange.dydx.trading.common.navigation.dydxComposable
import exchange.dydx.trading.feature.transfer.deposit.DydxTransferInstantDepositView
import exchange.dydx.trading.feature.transfer.deposit.DydxTransferTurnkeyDepositView
import exchange.dydx.trading.feature.transfer.deposit.qrcode.DydxTurnkeyQRCodeView
import exchange.dydx.trading.feature.transfer.faucet.DydxTransferFaucetView
import exchange.dydx.trading.feature.transfer.fiat.DydxFiatDepositView
import exchange.dydx.trading.feature.transfer.noble.DydxTransferNobleAddressView
import exchange.dydx.trading.feature.transfer.search.DydxInstantDepositSearchView
import exchange.dydx.trading.feature.transfer.search.DydxTransferSearchView
import exchange.dydx.trading.feature.transfer.selector.DydxTransferSelectorView
import exchange.dydx.trading.feature.transfer.status.DydxTransferInstantStatusView
import exchange.dydx.trading.feature.transfer.status.DydxTransferStatusView
import exchange.dydx.trading.feature.transfer.transferout.DydxTransferOutView
import exchange.dydx.trading.feature.transfer.withdrawal.DydxTransferWithdrawalView
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
        route = TransferRoutes.transfer_selector,
        deepLinks = appRouter.deeplinks(TransferRoutes.transfer_selector),
    ) { navBackStackEntry ->
        DydxTransferSelectorView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = TransferRoutes.transfer_deposit,
        deepLinks = appRouter.deeplinks(TransferRoutes.transfer_deposit),
    ) { navBackStackEntry ->
        DydxTransferInstantDepositView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = TransferRoutes.transfer_turnkey_deposit,
        deepLinks = appRouter.deeplinks(TransferRoutes.transfer_turnkey_deposit),
    ) { navBackStackEntry ->
        DydxTransferTurnkeyDepositView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = TransferRoutes.transfer_turnkey_qrcode + "/{chain}",
        arguments = listOf(navArgument("chain") { type = NavType.StringType }),
        deepLinks = appRouter.deeplinks(
            destination = TransferRoutes.transfer_turnkey_qrcode,
            path = "chain",
        ),
    ) { navBackStackEntry ->
        val chain = navBackStackEntry.arguments?.getString("chain")
        if (chain == null) {
            logger.e(TAG, "No chain passed")
            appRouter.navigateBack()
            return@dydxComposable
        }
        DydxTurnkeyQRCodeView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = TransferRoutes.transfer_fiat_deposit,
        deepLinks = appRouter.deeplinks(TransferRoutes.transfer_fiat_deposit),
    ) { navBackStackEntry ->
        DydxFiatDepositView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = TransferRoutes.transfer_withdrawal,
        deepLinks = appRouter.deeplinks(TransferRoutes.transfer_withdrawal),
    ) { navBackStackEntry ->
        DydxTransferWithdrawalView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = TransferRoutes.transfer_out,
        deepLinks = appRouter.deeplinks(TransferRoutes.transfer_out),
    ) { navBackStackEntry ->
        DydxTransferOutView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = TransferRoutes.transfer_faucet,
        deepLinks = appRouter.deeplinks(TransferRoutes.transfer_faucet),
    ) { navBackStackEntry ->
        DydxTransferFaucetView.Content(Modifier)
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
        route = TransferRoutes.transfer_deposit_search,
        deepLinks = appRouter.deeplinks(TransferRoutes.transfer_deposit_search),
    ) { navBackStackEntry ->
        DydxInstantDepositSearchView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = TransferRoutes.transfer_deposit_noble,
        deepLinks = appRouter.deeplinks(TransferRoutes.transfer_deposit_noble),
    ) { navBackStackEntry ->
        DydxTransferNobleAddressView.Content(Modifier)
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

    dydxComposable(
        router = appRouter,
        route = TransferRoutes.transfer_status_instant + "/{hash}",
        arguments = listOf(navArgument("hash") { type = NavType.StringType }),
        deepLinks = appRouter.deeplinks(
            destination = TransferRoutes.transfer_status_instant,
            path = "hash",
        ),
    ) { navBackStackEntry ->
        val hash = navBackStackEntry.arguments?.getString("hash")
        if (hash == null) {
            logger.e(TAG, "No hash passed")
            appRouter.navigateTo(TransferRoutes.transfer)
            return@dydxComposable
        }
        DydxTransferInstantStatusView.Content(Modifier)
    }
}
