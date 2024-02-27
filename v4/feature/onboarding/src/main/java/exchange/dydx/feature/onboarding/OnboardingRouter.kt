package exchange.dydx.feature.onboarding

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import exchange.dydx.feature.onboarding.connect.DydxOnboardConnectView
import exchange.dydx.feature.onboarding.debugscan.DydxDebugScanView
import exchange.dydx.feature.onboarding.desktopscan.DydxDesktopScanView
import exchange.dydx.feature.onboarding.tos.DydxTosView
import exchange.dydx.feature.onboarding.walletlist.DydxWalletListView
import exchange.dydx.feature.onboarding.welcome.DydxOnboardWelcomeView
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.common.navigation.dydxComposable

private const val TAG: String = "LoginRoute"

fun NavGraphBuilder.loginGraph(
    appRouter: DydxRouter,
) {
    dydxComposable(
        router = appRouter,
        route = OnboardingRoutes.welcome,
        deepLinks = appRouter.deeplinks(OnboardingRoutes.welcome),
    ) { nbse ->
        DydxOnboardWelcomeView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = OnboardingRoutes.wallet_list,
        deepLinks = appRouter.deeplinks(OnboardingRoutes.wallet_list),
    ) { nbse ->
        DydxWalletListView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = OnboardingRoutes.connect + "/{walletId}",
        arguments = listOf(navArgument("walletId") { type = NavType.StringType }),
        deepLinks = appRouter.deeplinksWithParam(OnboardingRoutes.connect, "walletId"),
    ) { nbse ->
        DydxOnboardConnectView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = OnboardingRoutes.desktop_scan,
        deepLinks = appRouter.deeplinks(OnboardingRoutes.desktop_scan),
    ) { nbse ->
        DydxDesktopScanView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = OnboardingRoutes.debug_scan,
        deepLinks = appRouter.deeplinks(OnboardingRoutes.debug_scan),
    ) { nbse ->
        DydxDebugScanView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = OnboardingRoutes.tos,
        deepLinks = appRouter.deeplinks(OnboardingRoutes.tos),
    ) { nbse ->
        DydxTosView.Content(Modifier)
    }
}
