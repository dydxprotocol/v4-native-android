package exchange.dydx.vault

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.VaultRoutes
import exchange.dydx.trading.common.navigation.VaultRoutes.confirmation
import exchange.dydx.trading.common.navigation.dydxComposable
import exchange.dydx.trading.feature.shared.bottombar.DydxBottomBarScaffold
import exchange.dydx.trading.feature.vault.DydxVaultView
import exchange.dydx.trading.feature.vault.depositwithdraw.DydxVaultDepositWithdrawView
import exchange.dydx.trading.feature.vault.depositwithdraw.confirmation.DydxVaultConfirmationView
import exchange.dydx.trading.feature.vault.history.DydxVaultHistoryView
import exchange.dydx.trading.feature.vault.tos.DydxVaultTosView
import exchange.dydx.utilities.utils.Logging

fun NavGraphBuilder.vaultGraph(
    appRouter: DydxRouter,
    logger: Logging,
) {
    dydxComposable(
        router = appRouter,
        route = VaultRoutes.main,
        deepLinks = appRouter.deeplinks(VaultRoutes.main),
    ) { navBackStackEntry ->
        DydxBottomBarScaffold(Modifier) {
            DydxVaultView.Content(Modifier)
        }
    }

    dydxComposable(
        router = appRouter,
        route = VaultRoutes.deposit,
        deepLinks = appRouter.deeplinks(VaultRoutes.deposit),
    ) { navBackStackEntry ->
        DydxVaultDepositWithdrawView.Content(Modifier, type = DydxVaultDepositWithdrawView.DepositWithdrawType.DEPOSIT)
    }

    dydxComposable(
        router = appRouter,
        route = VaultRoutes.withdraw,
        deepLinks = appRouter.deeplinks(VaultRoutes.withdraw),
    ) { navBackStackEntry ->
        DydxVaultDepositWithdrawView.Content(Modifier, type = DydxVaultDepositWithdrawView.DepositWithdrawType.WITHDRAW)
    }

    dydxComposable(
        router = appRouter,
        route = VaultRoutes.confirmation,
        deepLinks = appRouter.deeplinks(VaultRoutes.confirmation),
    ) { navBackStackEntry ->
        DydxVaultConfirmationView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = VaultRoutes.history,
        deepLinks = appRouter.deeplinks(VaultRoutes.history),
    ) { navBackStackEntry ->
        DydxVaultHistoryView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = VaultRoutes.tos,
        deepLinks = appRouter.deeplinks(VaultRoutes.tos),
    ) { navBackStackEntry ->
        DydxVaultTosView.Content(Modifier)
    }
}
