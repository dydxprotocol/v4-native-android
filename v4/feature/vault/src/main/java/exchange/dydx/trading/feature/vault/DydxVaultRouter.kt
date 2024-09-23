package exchange.dydx.vault

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.VaultRoutes
import exchange.dydx.trading.common.navigation.dydxComposable
import exchange.dydx.trading.feature.shared.bottombar.DydxBottomBarScaffold
import exchange.dydx.trading.feature.vault.DydxVaultView
import exchange.dydx.trading.feature.vault.depositwithdraw.DydxVaultDepositWithdrawView
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
        DydxBottomBarScaffold(Modifier) {
            DydxVaultDepositWithdrawView.Content(Modifier, type = DydxVaultDepositWithdrawView.DepositWithdrawType.DEPOSIT)
        }
    }

    dydxComposable(
        router = appRouter,
        route = VaultRoutes.withdraw,
        deepLinks = appRouter.deeplinks(VaultRoutes.withdraw),
    ) { navBackStackEntry ->
        DydxBottomBarScaffold(Modifier) {
            DydxVaultDepositWithdrawView.Content(Modifier, type = DydxVaultDepositWithdrawView.DepositWithdrawType.WITHDRAW)
        }
    }
}
